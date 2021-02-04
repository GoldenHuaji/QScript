/* QScript - An Xposed module to run scripts on QQ
 * Copyright (C) 2021-20222 chinese.he.amber@gmail.com
 * https://github.com/GoldenHuaji/QScript
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
#include <jni.h>
#include <string>
#include <unistd.h>
#include <android/log.h>
extern "C"
JNIEXPORT jstring JNICALL
Java_tsihen_me_qscript_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "QScript: 一个 Xposed 模块";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jint JNICALL
Java_tsihen_me_qscript_util_Natives_getpagesize(JNIEnv *env, jobject thiz) {
    return getpagesize();
}

typedef struct tm timeStruct;

/**
 * @brief getDateFromMacro
 * @param time __DATE__
 * @return
 */
static timeStruct getDateFromMacro(char const *time) {
    char s_month[5];
    int month, day, year;
    timeStruct t = {0};
    static const char month_names[] = "JanFebMarAprMayJunJulAugSepOctNovDec";

    sscanf(time, "%s %d %d", s_month, &day, &year);

    month = (strstr(month_names, s_month) - month_names) / 3;

    t.tm_mon = month;
    t.tm_mday = day;
    t.tm_year = year - 1900;
    t.tm_isdst = -1;

    return t;
}

/**
 * @brief getTimeFromMacro
 * @param time __TIME__
 * @return
 */
static timeStruct getTimeFromMacro(char const *time) {
    int hour, min, sec;
    timeStruct t = {0};

    sscanf(time, "%d:%d:%d", &hour, &min, &sec);

    t.tm_hour = hour;
    t.tm_min = min;
    t.tm_sec = sec;

    return t;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_tsihen_me_qscript_util_Utils_ntGetBuildTimestamp(JNIEnv *env, jclass clazz) {
    timeStruct date = getDateFromMacro(__DATE__);
    timeStruct time = getTimeFromMacro(__TIME__);
    timeStruct t = {0};

    t.tm_sec = time.tm_sec;
    t.tm_min = time.tm_min;
    t.tm_hour = time.tm_hour;
    t.tm_mon = date.tm_mon;
    t.tm_mday = date.tm_mday;
    t.tm_year = date.tm_year;
    t.tm_isdst = date.tm_isdst;

    long long finalTime = ((long long) mktime(&t)) * 1000;

    return finalTime;
}
