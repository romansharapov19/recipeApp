package com.projectssc.recipeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.projectssc.recipeapp.database.RecipeDatabase
import com.projectssc.recipeapp.entities.Category
import com.projectssc.recipeapp.entities.Meal
import com.projectssc.recipeapp.entities.MealsItems
import com.projectssc.recipeapp.interfaces.GetDataService
import com.projectssc.recipeapp.retrofitclient.RetrofitClientInstance
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : BaseActivity(), EasyPermissions.RationaleCallbacks,
    EasyPermissions.PermissionCallbacks {
    private var READ_STORAGE_PERM = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        readStorageTask()

        btnRecipes.setOnClickListener {
            var intent = Intent(this@MainActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnFit.setOnClickListener {
            var intent = Intent(this@MainActivity, FitActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    fun getCategories() {
        val service = RetrofitClientInstance.retrofitInstance!!.create(GetDataService::class.java)
        val call = service.getCategoryList()
        call.enqueue(object : Callback<Category> {
            override fun onFailure(call: Call<Category>, t: Throwable) {

                Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(
                call: Call<Category>,
                response: Response<Category>
            ) {

                for (arr in response.body()!!.categorieitems!!) {
                    getMeal(arr.strcategory)
                }
                insertDataIntoRoomDb(response.body())
            }

        })
    }


    fun getMeal(categoryName: String) {
        val service = RetrofitClientInstance.retrofitInstance!!.create(GetDataService::class.java)
        val call = service.getMealList(categoryName)
        call.enqueue(object : Callback<Meal> {
            override fun onFailure(call: Call<Meal>, t: Throwable) {

                loader.visibility = View.INVISIBLE
                Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(
                call: Call<Meal>,
                response: Response<Meal>
            ) {

                insertMealDataIntoRoomDb(categoryName, response.body())
            }

        })
    }

    fun insertDataIntoRoomDb(category: Category?) {

        launch {
            this.let {

                for (arr in category!!.categorieitems!!) {
                    RecipeDatabase.getDatabase(this@MainActivity)
                        .recipeDao().insertCategory(arr)
                }
            }
        }
    }

    fun insertMealDataIntoRoomDb(categoryName: String, meal: Meal?) {

        launch {
            this.let {


                for (arr in meal!!.mealsItem!!) {
                    var mealItemModel = MealsItems(
                        arr.id,
                        arr.idMeal,
                        categoryName,
                        arr.strMeal,
                        arr.strMealThumb
                    )
                    RecipeDatabase.getDatabase(this@MainActivity)
                        .recipeDao().insertMeal(mealItemModel)
                    Log.d("mealData", arr.toString())
                }

                btnRecipes.visibility = View.VISIBLE
            }
        }
    }

    fun clearDataBase() {
        launch {
            this.let {
                RecipeDatabase.getDatabase(this@MainActivity).recipeDao().clearDb()
            }
        }
    }

    private fun hasReadStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private fun readStorageTask() {
        if (hasReadStoragePermission()) {
            clearDataBase()
            getCategories()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "This app needs access to your storage,",
                READ_STORAGE_PERM,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onRationaleDenied(requestCode: Int) {

    }

    override fun onRationaleAccepted(requestCode: Int) {

    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {

    }
}