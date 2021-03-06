/* QNotified - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2020 xenonhydride@gmail.com
 * https://github.com/ferredoxin/QNotified
 *
 * This software is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see
 * <https://www.gnu.org/licenses/>.
 */

@file:JvmName("Utils")
@file:Suppress("DEPRECATION")

package me.tsihen.qscript.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.Process.myPid
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.robv.android.xposed.XposedBridge
import me.tsihen.qscript.BuildConfig
import me.tsihen.qscript.config.ConfigManager
import java.io.*
import java.text.DateFormat
import java.text.DateFormat.getDateTimeInstance
import java.util.*

var debugMode: Boolean = false

fun initDebugMode() {
    val mgr = ConfigManager.tryGetDefaultConfig()
    debugMode = mgr?.getOrDefault("debug_mode", false) ?: false
}

// Log
fun appendToFile(fileName: String, content: String?) {
    val f = File(fileName)
    f.writeText(f.readText() + content)
}

fun Throwable.getStackTraceWithoutSystemAndMsg(): String {
    val str = Log.getStackTraceString(this)
    val reader = BufferedReader(StringReader(str))
    val res = java.lang.StringBuilder(javaClass.simpleName)
    res.append(":$message")
    res.append('\n')
    var count = 0
    if (this !is Error) {
        while (reader.readLine() != null) {
            val r = reader.readLine()
            if (r.contains(Regex("\\s*at android\\."))
                || r.contains(Regex("\\s*at com\\.android\\."))
                || r.contains(Regex("\\s*at java\\."))
            ) {
                continue
            }
            count += 1
            if (count >= 40) {
                res.append("推测：栈溢出，停止\n")
                break
            }
            res.append("$r\n")
        }
        return res.toString()
    } else return str
}

@JvmOverloads
fun log(e: Throwable, noFile: Boolean = false) {
    loge(e.getStackTraceWithoutSystemAndMsg(), noFile)
}

@JvmOverloads
fun loge(msg: String, noFile: Boolean = false) {
    try {
        XposedBridge.log(msg)
    } catch (e: NoClassDefFoundError) {
    }
    Log.e(QS_LOG_TAG, msg)
    try {
        @FromQNotified
        val path = Environment.getExternalStorageDirectory().absolutePath + "/me.tsihen.qscript.log"
        val f = File(path)
        try {
            if (!f.exists()) f.createNewFile()
            appendToFile(
                path,
                "[" +
                        getDateTimeInstance(
                            DateFormat.MEDIUM,
                            DateFormat.MEDIUM
                        ).format(Date(System.currentTimeMillis())) + " " + myPid() + "]E/ " + msg + "\n"
            )
        } catch (e: IOException) {
        }
    } catch (e: Exception) {
    }

    if (noFile) return
    // 错误日志需要保存
    val mgr = ConfigManager.tryGetDefaultConfig() ?: return
    mgr["has_error"] = true
    // 日志过多时，清理一下
    if ((mgr["error_message"] as? String?)?.length ?: 0 > 10000) mgr["error_message"] =
        mgr["error_message"]?.toString()?.substring(8000) ?: ""
    mgr["error_message"] = (mgr["error_message"]?.toString() ?: "") + "[" +
            getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.MEDIUM
            ).format(Date(System.currentTimeMillis())) + " " + myPid() + "]E/ " + msg + "\n"
}

fun logd(msg: String) {
    if (!debugMode && !BuildConfig.DEBUG) {
        return
    }
    try {
        XposedBridge.log(msg)
    } catch (e: NoClassDefFoundError) {
    }
    Log.d(QS_LOG_TAG, msg)
    try {
        val path = Environment.getExternalStorageDirectory().absolutePath + "/me.tsihen.qscript.log"
        val f = File(path)
        try {
            if (!f.exists()) f.createNewFile()
            appendToFile(
                path,
                "[" +
                        getDateTimeInstance(
                            DateFormat.MEDIUM,
                            DateFormat.MEDIUM
                        ).format(Date(System.currentTimeMillis())) + " " + myPid() + "]D/ " + msg + "\n"
            )
        } catch (e: IOException) {
        }
    } catch (e: Exception) {
    }
}

fun logi(msg: String) {
    try {
        XposedBridge.log(msg)
    } catch (e: NoClassDefFoundError) {
    }
    Log.i(QS_LOG_TAG, msg)
    try {
        val path = Environment.getExternalStorageDirectory().absolutePath + "/me.tsihen.qscript.log"
        val f = File(path)
        try {
            if (!f.exists()) f.createNewFile()
            appendToFile(
                path,
                "[" +
                        getDateTimeInstance(
                            DateFormat.MEDIUM,
                            DateFormat.MEDIUM
                        ).format(Date(System.currentTimeMillis())) + " " + myPid() + "]I/ " + msg + "\n"
            )
        } catch (e: IOException) {
        }
    } catch (e: Exception) {
    }
}

fun logw(msg: String) {
    try {
        XposedBridge.log(msg)
    } catch (e: NoClassDefFoundError) {
    }
    Log.w(QS_LOG_TAG, msg)
    try {
        val path = Environment.getExternalStorageDirectory().absolutePath + "/me.tsihen.qscript.log"
        val f = File(path)
        try {
            if (!f.exists()) f.createNewFile()
            appendToFile(
                path,
                "[" +
                        getDateTimeInstance(
                            DateFormat.MEDIUM,
                            DateFormat.MEDIUM
                        ).format(Date(System.currentTimeMillis())) + " " + myPid() + "]W/ " + msg + "\n"
            )
        } catch (e: IOException) {
        }
    } catch (e: Exception) {
    }
}

fun logv(msg: String) {
    if (debugMode) {
        try {
            XposedBridge.log(msg)
        } catch (ignored: NoClassDefFoundError) {
        }
    }
    Log.v(QS_LOG_TAG, msg)
}

fun scriptLog(e: Throwable) {
    scriptLog(e.getStackTraceWithoutSystemAndMsg())
}

fun scriptLog(msg: String) {
    try {
        XposedBridge.log("QScript : $msg")
    } catch (e: NoClassDefFoundError) {
    }
    Log.i(QS_LOG_TAG, "QScript : $msg")
    try {
        val path =
            Environment.getExternalStorageDirectory().absolutePath + "/me.tsihen.qscript_scripts.log"
        val f = File(path)
        try {
            if (!f.exists()) f.createNewFile()
            appendToFile(
                path,
                "[" +
                        getDateTimeInstance(
                            DateFormat.MEDIUM,
                            DateFormat.MEDIUM
                        ).format(Date(System.currentTimeMillis())) + " " + myPid() + "]/ " + msg + "\n"
            )
        } catch (e: IOException) {
        }
    } catch (e: Exception) {
    }
}

// log end

fun getActiveModuleVersion(): String? {
    Log.v("FAKE", "FAKE")
    return null
}


/* QNotified - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2020 xenonhydride@gmail.com
 * https://github.com/ferredoxin/QNotified
 *
 * This software is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see
 * <https://www.gnu.org/licenses/>.
 */
@FromQNotified
@SuppressLint("PrivateApi")
fun getCurrentActivity(): Activity? {
    try {
        val activityThreadClass =
            Class.forName("android.app.ActivityThread")
        val activityThread =
            activityThreadClass.getMethod("currentActivityThread").invoke(null)
        val activitiesField =
            activityThreadClass.getDeclaredField("mActivities")
        activitiesField.isAccessible = true
        val activities =
            activitiesField[activityThread] as Map<*, *>
        activities.values.forEach {
            val activityRecordClass: Class<*> = it!!.javaClass
            val pausedField =
                activityRecordClass.getDeclaredField("paused")
            pausedField.isAccessible = true
            if (!pausedField.getBoolean(it)) {
                val activityField =
                    activityRecordClass.getDeclaredField("activity")
                activityField.isAccessible = true
                return activityField[it] as Activity
            }
        }
    } catch (e: java.lang.Exception) {
        log(e)
    }
    return null
}

private external fun ntGetBuildTimestamp(): Long

/* QNotified - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2020 xenonhydride@gmail.com
 * https://github.com/ferredoxin/QNotified
 *
 * This software is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see
 * <https://www.gnu.org/licenses/>.
 */
@FromQNotified
fun getBuildTimestamp(context: Context? = null): Long {
    var ctx: Context? = context
    if (ctx == null) {
        try {
            ctx = getQQApplication()
        } catch (ignored: Throwable) {
        }
        if (ctx == null) {
            ctx = getCurrentActivity()
        }
    }
    if (ctx == null) {
        logd("Context is null.")
    }
    return try {
        Natives.load(ctx!!)
        ntGetBuildTimestamp()
    } catch (throwable: Throwable) {
        log(throwable)
        -3
    }
}

fun paramsTypesToString(vararg c: Class<*>?): String {
    if (c.isEmpty()) return "()"
    val sb = StringBuilder("(")
    for (i in c.indices) {
        sb.append(if (c[i] == null) "[null]" else c[i]!!.name)
        if (i != c.size - 1) {
            sb.append(",")
        }
    }
    sb.append(")")
    return sb.toString()
}

@FromQNotified
@Throws(java.lang.Exception::class)
fun copy(s: File, f: File) {
    if (!s.exists()) throw FileNotFoundException("源文件不存在")
    if (!f.exists()) f.createNewFile()
    val fr = FileReader(s)
    val fw = FileWriter(f)
    val buff = CharArray(1024)
    var len = 0
    while (len != -1) {
        fw.write(buff, 0, len)
        len = fr.read(buff)
    }
    fw.close()
    fr.close()
}

/**
 * 获取精确到秒的时间戳
 * @return
 */
fun getSecondTimestamp(date: Date?): Int {
    if (null == date) {
        return 0
    }
    val timestamp = java.lang.String.valueOf(date.time)
    val length = timestamp.length
    return if (length > 3) {
        Integer.valueOf(timestamp.substring(0, length - 3))
    } else {
        0
    }
}

inline fun <reified T : Activity> Context.startActivity() {
    val intent = Intent(this, Initiator.load(".activity.JumpActivity"))
    intent.putExtra(JUMP_ACTION_CMD, JUMP_ACTION_START_ACTIVITY)
        .putExtra(JUMP_ACTION_TARGET, T::class.java.name)

    this.startActivity(intent)
}

inline fun <reified T> Gson.fromJson(json: String): T? = this.fromJson<T>(
    json,
    object : TypeToken<T>() {}.type
)