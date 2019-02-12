package com.warchaser.glsurfaceviewdev.util

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.warchaser.glsurfaceviewdev.BuildConfig

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*


object NLog {

    private const val DELETE_DAYS_DURATION : Int = 5

    private val LINE_SEPARATOR : String = System.getProperty("line.separator")
    private val IS_DEBUG : Boolean = BuildConfig.DEBUG
    private val ALLOW_WRITE_LOG : Boolean = BuildConfig.DEBUG
    private const val JSON_RESULT = "<--[NetWork Response Result]-->"
    private var LOG_PATH : String = ""
    private const val FATAL : String = "FATAL"

    /**
     * 今天日期
     * */
    private var mTodayDate : Date? = null

    /**
     * 日期格式
     * 到日
     * */
    private val DATE_FORMAT_DAY : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * 日期格式
     * 到秒
     * */
    private val DATE_FORMAT_SS : SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)

    @JvmStatic
    @Synchronized
    fun i(tag : String, msg : String){
        if(IS_DEBUG){
            Log.i(tag, msg)
        }
    }

    @JvmStatic
    @Synchronized
    fun e (tag : String, msg : String){
        if(IS_DEBUG){
            Log.e(tag, msg)
        }
    }

    @JvmStatic
    @Synchronized
    fun d(tag: String, msg: String){
        if(IS_DEBUG){
            Log.d(tag, msg)
        }
    }

    @JvmStatic
    @Synchronized
    fun v(tag: String, msg: String){
        if(IS_DEBUG){
            Log.v(tag, msg)
        }
    }

    @JvmStatic
    @Synchronized
    fun w(tag : String, msg : String){
        if(IS_DEBUG){
            Log.w(tag, msg)
        }
    }

    @JvmStatic
    @Synchronized
    fun writeErrLog(tag : String, msg : String){
        if(ALLOW_WRITE_LOG){
            writeLog2File(tag, msg)
        }
    }

    /**
     * 将Throwable打印至logcat并根据开关写文件
     * */
    @JvmStatic
    @Synchronized
    fun printStackTrace(tag : String, e : Throwable){
        if(IS_DEBUG){
            e(tag, FATAL)
            e(tag, e::class.java.name)
            val message = e.message
            if(message != null){
                e(tag, message)
            }
            for(string in e.stackTrace){
                e(tag, string.toString())
            }
        } else {
            e.printStackTrace()
        }

        if(ALLOW_WRITE_LOG){
            writeLog2File(tag, FATAL)
            writeLog2File(tag, e::class.java.name)
            val message = e.message
            if(message != null){
                writeLog2File(tag, message)
            }

            for(string in e.stackTrace){
                writeLog2File(tag, string.toString())
            }

            val cause = e.cause

            if(cause != null){
                for(string in cause.stackTrace){
                    writeLog2File(tag, string.toString())
                }
            }
        }
    }

    /**
     * 初始化
     * 写Log文件
     * */
    @JvmStatic
    fun initLogFile(context : Context){
        LOG_PATH = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {// 优先保存到SD卡中
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).absolutePath + File.separator + context.packageName + File.separator + "Log" + File.separator
        } else {// 如果SD卡不存在，就保存到本应用的目录下
            context.filesDir.absolutePath + File.separator + context.packageName + File.separator + "Log" + File.separator
        }

        val file = File(LOG_PATH)
        if (!file.exists()) {
            file.mkdirs()
        }

        mTodayDate = Date()

        //删除N天前的log，N由DELETE_DAYS_DURATION决定
        Thread(Runnable {
            try {
                deleteOldFiles(DELETE_DAYS_DURATION, getFiles(LOG_PATH)!!, mTodayDate!!)
            } catch (e : Exception){
                printStackTrace("DELETE_LOGS", e)
            } catch (e : Error){
                printStackTrace("DELETE_LOGS", e)
            }
        }).start()
    }

    @Synchronized
    private fun getLogPath() : String{
        return LOG_PATH
    }

    /**
     * 写Log文件
     * */
    @JvmStatic
    @Synchronized
    private fun writeLog2File(tag : String, msg : String){
        if(TextUtils.isEmpty(getLogPath())){
            e(tag, "LOG_PATH is empty!")
            return
        }

        val file = File(LOG_PATH)

        if(!file.exists()){
            file.mkdirs()
        }

        val fileName = getLogPath() + DATE_FORMAT_DAY.format(mTodayDate) + ".log"
        var fos : FileOutputStream? = null
        var bw : BufferedWriter? = null
        val time : String = DATE_FORMAT_SS.format(Date())
        try {
            //这里的第二个参数代表追加还是覆盖，true为追加，false为覆盖
            fos = FileOutputStream(fileName, true)
            bw = BufferedWriter(OutputStreamWriter(fos))
            bw.write("$time ")
            bw.write("$tag: ")
            bw.write(msg)
            bw.write(LINE_SEPARATOR)
        } catch (e : Exception) {
            e.printStackTrace()
        } catch (e : Error) {
            e.printStackTrace()
        } finally {
            try {
                bw?.close()
                fos?.close()
            } catch (e : Exception){
                e.printStackTrace()
            } catch (e : Error){
                e.printStackTrace()
            }
        }
    }

    @JvmStatic
    private fun getFiles(path: String): Array<File>? {
        val file = File(path)
        if (!file.exists()){
            return null
        }
        return file.listFiles()
    }

    @JvmStatic
    private fun deleteOldFiles(days : Int, files : Array<File>, todayDate : Date){
        if(files == null || files.isEmpty()){
           return
        }

        for(file in files){
            val fileName = file.name
            val realFileName = fileName.substring(0, fileName.lastIndexOf("."))
            if(daysBetween(DATE_FORMAT_DAY.parse(realFileName), todayDate) >= days){
                file.delete()
            }
        }
    }

    private fun daysBetween(date1: Date, date2: Date): Int {
        val cal = Calendar.getInstance()
        cal.time = date1
        val time1 = cal.timeInMillis
        cal.time = date2
        val time2 = cal.timeInMillis
        val betweenDays = (time2 - time1) / (1000 * 3600 * 24)

        return Integer.parseInt(betweenDays.toString())
    }

    @JvmStatic
    fun printLine(tag : String, isTop : Boolean){
        if(isTop){
            e(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════")
        } else {
            e(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════")
        }
    }

    /**
     * 打印json至logcat
     * */
    @JvmStatic
    @Synchronized
    fun printJson(tag : String, msg : String){

        var message : String

        try {
            message = when {
                msg.startsWith("{") -> {
                    val jsonObject = JSONObject(msg)
                    //最重要的方法,就一行,返回格式化的json字符串,其中的数字4是缩进字符数
                    jsonObject.toString(4)
                }
                msg.startsWith("[") -> {
                    val jsonArray = JSONArray(msg)
                    jsonArray.toString(4)
                }
                else -> msg
            }
        } catch (e : Exception){
            message = msg
            e.printStackTrace()
        } catch (e : Error){
            message = msg
            e.printStackTrace()
        }

//        printLine(tag, true)
        message = JSON_RESULT + LINE_SEPARATOR + message
        val lines : List<String> = message.split(LINE_SEPARATOR)
        for (line : String in lines){
            e(tag, line)
        }
//        printLine(tag, false)

    }
}
