//
//   Calendar Notifications Plus
//   Copyright (C) 2016 Sergey Parshin (s.parshin.sc@gmail.com)
//
//   This program is free software; you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation; either version 3 of the License, or
//   (at your option) any later version.
//
//   This program is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.
//
//   You should have received a copy of the GNU General Public License
//   along with this program; if not, write to the Free Software Foundation,
//   Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
//

package com.github.quarck.gmail2calhk
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.os.Vibrator
import android.widget.TimePicker

//import com.github.quarck.calnotify.logs.Logger


@Suppress("UNCHECKED_CAST")
fun <T> Context.service(svc: String) = getSystemService(svc) as T

val Context.alarmManager: AlarmManager
    get() = service(Context.ALARM_SERVICE)

val Context.audioManager: AudioManager
    get() = service(Context.AUDIO_SERVICE)

val Context.powerManager: PowerManager
    get() = service(Context.POWER_SERVICE)

val Context.vibratorService: Vibrator
    get() = service(Context.VIBRATOR_SERVICE)

val Context.notificationManager: NotificationManager
    get() = service(Context.NOTIFICATION_SERVICE)


val isMarshmallowOrAbove: Boolean
    get() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M

val isLollipopOrAbove: Boolean
    get() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP

val isKitkatOrAbove: Boolean
    get() = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT

@Suppress("DEPRECATION")
var TimePicker.hourCompat: Int
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            this.hour
        else
            this.currentHour
    }
    set(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            this.hour = value
        else
            this.currentHour = value
    }

@Suppress("DEPRECATION")
var TimePicker.minuteCompat: Int
    get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            this.minute
        else
            this.currentMinute
    }
    set(value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            this.minute = value
        else
            this.currentMinute = value
    }

val Exception.detailed: String
    get() {
        return "${this}: ${this.message}, stack: ${this.stackTrace.joinToString("\n")}"
    }