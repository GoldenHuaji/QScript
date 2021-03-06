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
package me.tsihen.qscript.script.qscript

import me.tsihen.qscript.script.qscript.objects.MemberJoinData
import me.tsihen.qscript.script.qscript.objects.MessageData

object QScriptEventSender {
    /**
     * 广播消息事件
     */
    fun doOnMsg(data: MessageData) {
        QScriptManager.getScripts().forEach {
            if (!it.isEnable()) return@forEach
            it.onMsg(data)
        }
    }

    /**
     * 广播新成员入群事件
     */
    fun doOnJoin(data: MemberJoinData) {
        QScriptManager.getScripts().forEach {
            if (!it.isEnable()) return@forEach
            it.onJoin(data)
        }
    }
}
