package com.firozanwar.kotlincoroutines

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_levelone_basic.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

class BasicLevel1Activity : AppCompatActivity(), View.OnClickListener {

    val JOB_TIMEOUT = 1900L

    private val PROGRESS_MAX = 100
    private val PROGRESS_START = 0
    private val JOB_TIME = 4000 // ms
    lateinit var job: CompletableJob

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levelone_basic)

        btnClickMe.setOnClickListener(this)
        job_button.setOnClickListener(this)
    }

    override fun onClick(view: View?) {

        when (view?.id) {
            R.id.btnClickMe -> {
                CoroutineScope(Dispatchers.IO).launch {
                    //fakeApiRequest();     // Case 1
                    // fakeApiRequest2();   // Case 2
                    // fakeApiRequest3();   // Case 3A
                    //fakeApiRequest4();      // Case 3B
                    fakeApiRequest5() // Case 5
                }
            }

            R.id.job_button -> {
                if (!::job.isInitialized) {
                    initJob()
                }
                job_progress_bar.startJobOrCancel(job)
            }
        }
    }

    /************ Case:1 Basic operation using coroutines ************/
    /**
     * Fired one request and when it completes and another get fired.
     * here 2nd job waits for 1st one to complete.
     */
    private suspend fun fakeApiRequest() {

        var result1 = getResult1FromAPI();  // wait until job is done
        println("debug: $result1");
        setTextOnMainThread(result1);

        var result2 = getResult2FromAPI();  // wait until job is done
        println("debug: $result2");
        setTextOnMainThread(result2);
    }

    /**
     * Timeout handling in coroutines with job withTimeoutOrNull()
     * just check job as null
     */
    private suspend fun fakeApiRequest2() {

        withContext(Dispatchers.IO) {
            val job = withTimeoutOrNull(JOB_TIMEOUT) {
                var result1 = getResult1FromAPI();  // wait until job is done
                println("debug: $result1");
                setTextOnMainThread(result1);

                var result2 = getResult2FromAPI();  // wait until job is done
                println("debug: $result2");
                setTextOnMainThread(result2);
            }

            if (job == null) {
                val cancelMsgs = "debug: job cancelled due to timout $JOB_TIMEOUT"
                println(cancelMsgs)
                setTextOnMainThread(cancelMsgs);
            }
        }
    }

    private suspend fun setTextOnMainThread(input: String) {
        withContext(Dispatchers.Main) {
            setNewText(input);
        }
    }

    private fun setNewText(input: String) {
        val newText = tvResult.text.toString() + "\n$input"
        tvResult.text = newText
    }

    private suspend fun getResult1FromAPI(): String {
        logThread("getResult1FromAPI");
        delay(1000);
        return "Result1";
    }

    private suspend fun getResult2FromAPI(): String {
        logThread("getResult2FromAPI");
        //delay(1000); // Don't block the thread, just suspend the coroutines   // For case 1,2
        delay(1700); // For case 3,4 it is 1700
        return "Result2";
    }

    private suspend fun getResult2FromAPI(result1: String): String {
        logThread("getResult2FromAPI");
        delay(1700); // For case 3,4 it is 1700
        if (result1.equals("Result1")) {
            return "Result2";
        }
        throw CancellationException("Input was incorrect")
    }

    private fun logThread(methodName: String) {
        println("debug: $methodName : ${Thread.currentThread().name}")
    }

    /************ Case:2  Jobs in coroutines ************/
    fun initJob() {
        job_button.setText("Start job #1")
        updateJobCompleteTextView("")

        job = Job()
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if (msg.isNullOrEmpty()) {
                    msg = "Unknown cancellation error"
                }
                println("debug: $job was cancelled reason $msg")
                showToas(msg)
            }
        }

        job_progress_bar.max = PROGRESS_MAX
        job_progress_bar.progress = PROGRESS_START
    }

    fun showToas(text: String) {
        GlobalScope.launch(Dispatchers.Main) {
            Toast.makeText(this@BasicLevel1Activity, text, Toast.LENGTH_LONG).show()
        }
    }

    fun ProgressBar.startJobOrCancel(job: Job) {
        if (this.progress > 0) {
            println("debug: This $job is already active. Cancelling... ")
            resetJob()
        } else {
            job_button.setText("Cancel job #1")
            CoroutineScope(Dispatchers.IO + job).launch {
                println("debug: Coroutines $this is activated with $job")

                for (i in PROGRESS_START..PROGRESS_MAX) {
                    delay((JOB_TIMEOUT / PROGRESS_MAX).toLong())
                    this@startJobOrCancel.progress = i
                }
                updateJobCompleteTextView("Job is complete")
            }
        }
    }

    private fun updateJobCompleteTextView(input: String) {
        GlobalScope.launch(Dispatchers.Main) {
            job_complete_text.setText(input)
        }
    }

    fun resetJob() {
        if (job.isActive || job.isCompleted) {
            job.cancel(CancellationException("Resetting job"))
        }
        initJob()
    }

    /************ Case:3 PARALLEL Background Tasks with Kotlin Coroutines (ASYNC AND AWAIT) ************/

    // Case 3A : Using general without async and await..
    private suspend fun fakeApiRequest3() {
        val startTime = System.currentTimeMillis();
        val parentJob = CoroutineScope(Dispatchers.IO).launch {
            val job1 = launch {
                val time1 = measureTimeMillis {
                    println("debug: Launching job1 in ${Thread.currentThread().name}")
                    val result1 = getResult1FromAPI();
                    setTextOnMainThread("Got ${result1}")
                }
                println("debug: Completed job1 in $time1 ms ")
            }
            //job1.join() // -> Sequentially launch of job2 after job1 finishes

            val job2 = launch {
                val time2 = measureTimeMillis {
                    println("debug: Launching job2 in ${Thread.currentThread().name}")
                    val result2 = getResult2FromAPI();
                    setTextOnMainThread("Got ${result2}")
                }
                println("debug: Completed job2 in $time2 ms ")
            }
        }
        parentJob.invokeOnCompletion {
            println("debug: total elasped time to complete all jobs: ${System.currentTimeMillis() - startTime}")
        }
    }

    // Case 3B : Using general with async and await..
    private suspend fun fakeApiRequest4() {
        CoroutineScope(Dispatchers.IO).launch {
            val measureTime = measureTimeMillis {
                val result1: Deferred<String> = async {
                    println("debug: Launching job1 in ${Thread.currentThread().name}")
                    getResult1FromAPI()
                }

                val result2: Deferred<String> = async {
                    println("debug: Launching job2 in ${Thread.currentThread().name}")
                    getResult2FromAPI()
                }

                setTextOnMainThread("Got ${result1.await()}")
                setTextOnMainThread("Got ${result2.await()}")
            }
            println("debug: total time elapsed ${measureTime}")
        }
    }

    /************ Case:4 SEQUENTIAL Background Tasks with Kotlin Coroutines (async and await) ************/
    private suspend fun fakeApiRequest5() {
        CoroutineScope(Dispatchers.IO).launch {
            val measureTime = measureTimeMillis {
                val result1 = async {
                    println("debug: Launching job1 in ${Thread.currentThread().name}")
                    getResult1FromAPI()
                }.await()

                val result2 = async {
                    println("debug: Launching job2 in ${Thread.currentThread().name}")
                    try{
                        getResult2FromAPI(result1)
                        //getResult2FromAPI("ssfsfsfsdfsdzf")  // To check the exception
                    }catch (e:CancellationException){
                        e.message
                    }
                }.await()
                println("debug: Got result2 ${result2}")
            }
            println("debug: total time elapsed ${measureTime}")
        }
    }
}
