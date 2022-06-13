package com.projectssc.recipeapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.projectssc.recipeapp.adapter.MainCategoryAdapter
import com.projectssc.recipeapp.adapter.SubCategoryAdapter
import com.projectssc.recipeapp.database.RecipeDatabase
import com.projectssc.recipeapp.entities.CategoryItems
import com.projectssc.recipeapp.entities.MealsItems
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class HomeActivity : BaseActivity() {
    private val RQ_SPEECH_REC = 102
    var arrMainCategory = ArrayList<CategoryItems>()
    var arrSubCategory = ArrayList<MealsItems>()

    var mainCategoryAdapter = MainCategoryAdapter()
    var subCategoryAdapter = SubCategoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        back.setOnClickListener {
            var intent = Intent(this@HomeActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnMic.setOnClickListener {
            askSpeechInput()
        }

        getDataFromDb()

        mainCategoryAdapter.setClickListener(onCLicked)
        subCategoryAdapter.setClickListener(onCLickedSubItem)


        val closeBtnId =
            search_view.context.resources.getIdentifier("android:id/search_close_btn", null, null)
        val closeBtn = search_view.findViewById<ImageView>(closeBtnId)
        closeBtn?.setOnClickListener {
            getDataFromDb()
            search_view.setQuery("", false)
            search_view.clearFocus()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RQ_SPEECH_REC && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            search_view.setQuery(result?.get(0).toString(), true)
            launch {

                this.let {
                    var cat =
                        RecipeDatabase.getDatabase(this@HomeActivity).recipeDao().getAllCategory()
                    arrMainCategory = cat as ArrayList<CategoryItems>

                    var search: ArrayList<CategoryItems> = ArrayList()
                    for (categoryItems in arrMainCategory) {
                        if (categoryItems.strcategory.equals(
                                result?.get(0).toString().capitalize()
                            )
                        ) {
                            search.add(categoryItems)
                        }
                    }
                    arrMainCategory.reverse()

                    getMealDataFromDb(arrMainCategory[0].strcategory)
                    mainCategoryAdapter.setData(search)
                    rv_main_category.layoutManager =
                        LinearLayoutManager(
                            this@HomeActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    rv_main_category.adapter = mainCategoryAdapter

                    getMealDataFromDb(result?.get(0).toString().capitalize())
                }
            }
        }
    }

    private fun askSpeechInput() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition is not available.", Toast.LENGTH_SHORT).show()
        } else {
            val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            i.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            i.putExtra(RecognizerIntent.EXTRA_PROMPT, "What you want to eat?")
            startActivityForResult(i, RQ_SPEECH_REC)
        }
    }

    private val onCLicked = object : MainCategoryAdapter.OnItemClickListener {

        override fun onClicked(categoryName: String) {
            getMealDataFromDb(categoryName)
        }
    }


    private val onCLickedSubItem = object : SubCategoryAdapter.OnItemClickListener {
        override fun onClicked(id: String) {
            var intent = Intent(this@HomeActivity, DetailActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }
    }

    private fun getDataFromDb() {
        launch {
            this.let {
                var cat = RecipeDatabase.getDatabase(this@HomeActivity).recipeDao().getAllCategory()
                arrMainCategory = cat as ArrayList<CategoryItems>

                arrMainCategory.reverse()

                getMealDataFromDb(arrMainCategory[0].strcategory)
                mainCategoryAdapter.setData(arrMainCategory)
                rv_main_category.layoutManager =
                    LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
                rv_main_category.adapter = mainCategoryAdapter
            }
        }
    }

    private fun getMealDataFromDb(categoryName: String) {
        tvCategory.text = "$categoryName Category"
        launch {
            this.let {
                var cat = RecipeDatabase.getDatabase(this@HomeActivity).recipeDao()
                    .getSpecificMealList(categoryName)
                arrSubCategory = cat as ArrayList<MealsItems>
                subCategoryAdapter.setData(arrSubCategory)
                rv_sub_category.layoutManager =
                    LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
                rv_sub_category.adapter = subCategoryAdapter
            }
        }
    }
}