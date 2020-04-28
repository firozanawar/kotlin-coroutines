package com.firozanwar.kotlincoroutines

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_levelone_basic.*
import kotlinx.coroutines.*

class BasicLevel1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levelone_basic)

        btnClickMe.setOnClickListener {

            CoroutineScope(Dispatchers.IO).launch {
                fakeApiRequest();
            }
        }
    }

    private suspend fun fakeApiRequest() {

        var result1 = getResult1FromAPI();  // wait until job is done
        println("debug: $result1");
        setTextOnMainThread(result1);

        var result2 = getResult2FromAPI();  // wait until job is done
        println("debug: $result2");
        setTextOnMainThread(result2);
    }

    private suspend fun setTextOnMainThread(input:String){
        withContext(Dispatchers.Main){
            setNewText(input);
        }
    }

    private fun setNewText(input: String) {
        val newText = tvResult.text.toString() + "\n$input"
        tvResult.text = newText
    }

    private suspend fun getResult2FromAPI(): String {
        logThread("getResult2FromAPI");
        delay(1000);
        return "Result2";
    }

    private suspend fun getResult1FromAPI(): String {
        logThread("getResult1FromAPI");
        delay(1000);
        return "Result1";
    }

    private fun logThread(methodName: String) {
        println("debug: $methodName : ${Thread.currentThread().name}")
    }
}
