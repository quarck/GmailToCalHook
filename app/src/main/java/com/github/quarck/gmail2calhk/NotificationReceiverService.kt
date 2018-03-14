/*
 * Copyright (c) 2015, Sergey Parshin, s.parshin@outlook.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of developer (Sergey Parshin) nor the
 *       names of other project contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.quarck.gmail2calhk

import android.app.Notification
import android.content.Intent
import android.os.*
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.*

class NotificationReceiverService : NotificationListenerService()
{
	private val handledPackages = arrayOf<String>("com.google.android.gm")

	override fun onCreate()
	{
		super.onCreate()
	}

	override fun onDestroy()
	{
		super.onDestroy()
	}

	override fun onBind(intent: Intent): IBinder?
	{
		return super.onBind(intent)
	}

	override fun onNotificationPosted(notification: StatusBarNotification?)
	{
		if (notification != null)
		{
			val packageName = notification.packageName

			if (packageName in handledPackages)
			{
				val (title, text) = notification.notification.getTitleAndText()
				if ((title != "" && title.contains("todo:", ignoreCase = true))||
						(text != "" && text.contains("todo:", ignoreCase = true)))
				{
					// Default calendar
					val calendars = CalendarProvider
							.getCalendars(this)
							.filter {
								!it.isReadOnly &&  it.isVisible
							}

					if (calendars.isEmpty()) {
						return
					}

					val calendar = calendars.filter { it.isPrimary }.firstOrNull() ?: calendars[0]

					val ev = CalendarEventDetails (
							title=text + " #task",
							desc=title,
							location = "",
							timezone = TimeZone.getDefault().getID(),
							startTime = System.currentTimeMillis() + 4L * 3600L * 1000L,
							endTime = System.currentTimeMillis() + 5L * 3600L * 1000L,
							isAllDay = false,
							reminders = mutableListOf(EventReminderRecord(900*1000))
					)

					val st = this.persistentState

					if (st.lastDesc == ev.desc && st.lastTitle == ev.title) {
						// skip
					} else {
						st.lastDesc = ev.desc
						st.lastTitle = ev.title
						CalendarProvider.createEvent(this, calendar.calendarId, "", ev)
					}
				}
			}
		}
	}
}
