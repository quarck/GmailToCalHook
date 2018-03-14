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

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.CalendarContract

import java.util.*

data class CalendarRecord(
        val calendarId: Long,
        val owner: String,
        val displayName: String,
        val name: String,
        val accountName: String,
        val accountType: String,
        val timeZone: String,
        val color: Int,
        val isVisible: Boolean,
        val isPrimary: Boolean,
        val isReadOnly: Boolean,
        val isSynced: Boolean
)

object CalendarProvider  {
    private const val LOG_TAG = "CalendarProvider"

    fun createEvent(
            context: Context,
            calendarId: Long,
            calendarOwnerAccount: String,
            details: CalendarEventDetails
    ): Long {

        var eventId = -1L

        if (!PermissionsManager.hasAllPermissions(context)) {
            return -1
        }

        val values = ContentValues()

        values.put(CalendarContract.Events.TITLE, details.title)
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId)
        values.put(CalendarContract.Events.EVENT_TIMEZONE, details.timezone) // Irish summer time
        values.put(CalendarContract.Events.DESCRIPTION, details.desc)

        values.put(CalendarContract.Events.DTSTART, details.startTime)
        values.put(CalendarContract.Events.DTEND, details.endTime)

        values.put(CalendarContract.Events.EVENT_LOCATION, details.location)

        //

        if (details.color != 0)
            values.put(CalendarContract.Events.EVENT_COLOR, details.color) // just something

        values.put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_DEFAULT)
        values.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)

        values.put(CalendarContract.Events.HAS_ALARM, 1)
        values.put(CalendarContract.Events.ALL_DAY, if (details.isAllDay) 1 else 0)

        if (details.repeatingRule != "")
            values.put(CalendarContract.Events.RRULE, details.repeatingRule)
        if (details.repeatingRDate != "")
            values.put(CalendarContract.Events.RDATE, details.repeatingRDate)

        if (details.repeatingExRule != "")
            values.put(CalendarContract.Events.EXRULE, details.repeatingExRule)
        if (details.repeatingExRDate != "")
            values.put(CalendarContract.Events.EXDATE, details.repeatingExRDate)


        values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED)
        values.put(CalendarContract.Events.SELF_ATTENDEE_STATUS, CalendarContract.Events.STATUS_CONFIRMED)

        // https://gist.github.com/mlc/5188579
        values.put(CalendarContract.Events.ORGANIZER, calendarOwnerAccount)
        values.put(CalendarContract.Events.HAS_ATTENDEE_DATA, 1);

        try {
            val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

            // get the event ID that is the last element in the Uri
            eventId = uri.lastPathSegment.toLong()
        }
        catch (ex: SecurityException) {
        }

        if (eventId != -1L) {
            // Now add reminders
            for (reminder in details.reminders) {
                val reminderValues = ContentValues()
                reminderValues.put(CalendarContract.Reminders.MINUTES, (reminder.millisecondsBefore / MINUTE_IN_MILLISECONDS).toInt())

                reminderValues.put(CalendarContract.Reminders.EVENT_ID, eventId)

                reminderValues.put(CalendarContract.Reminders.METHOD, reminder.method)
//                if (reminder.isEmail)
//                    reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_EMAIL)
//                else
//                    reminderValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_DEFAULT)

                try {
                    context.contentResolver.insert(
                            CalendarContract.Reminders.CONTENT_URI,
                            reminderValues
                    )
                }
                catch (ex: SecurityException) {

                }
            }
        }

        return eventId
    }

    fun getCalendars(context: Context): List<CalendarRecord> {

        val ret = mutableListOf<CalendarRecord>()

        if (!PermissionsManager.hasReadCalendar(context)) {
            return ret
        }

        try {

            val fields = mutableListOf(
                    CalendarContract.Calendars._ID,
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Calendars.NAME,
                    CalendarContract.Calendars.OWNER_ACCOUNT,
                    CalendarContract.Calendars.ACCOUNT_NAME,
                    CalendarContract.Calendars.ACCOUNT_TYPE,
                    CalendarContract.Calendars.CALENDAR_COLOR,

                    CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                    CalendarContract.Calendars.CALENDAR_TIME_ZONE,
                    CalendarContract.Calendars.SYNC_EVENTS,
                    CalendarContract.Calendars.VISIBLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                fields.add(CalendarContract.Calendars.IS_PRIMARY)
            }

            val uri = CalendarContract.Calendars.CONTENT_URI

            val cursor = context.contentResolver.query(
                    uri, fields.toTypedArray(), null, null, null)

            while (cursor != null && cursor.moveToNext()) {

                // Get the field values
                val calID: Long? = cursor.getLong(0)
                val displayName: String? = cursor.getString(1)
                val name: String? = cursor.getString(2)
                val ownerAccount: String? = cursor.getString(3)
                val accountName: String? = cursor.getString(4)
                val accountType: String? = cursor.getString(5)
                val color: Int? = cursor.getInt(6)
                val accessLevel: Int? = cursor.getInt(7)
                val timeZone: String? = cursor.getString(8)
                val syncEvents: Int? = cursor.getInt(9)
                val visible: Int? = cursor.getInt(10)

                val isPrimary: Int? =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            cursor.getInt(11)
                        } else {
                            0
                        }

                val isEditable =
                        when(accessLevel ?: 0) {
                            CalendarContract.Calendars.CAL_ACCESS_CONTRIBUTOR -> true
                            CalendarContract.Calendars.CAL_ACCESS_OWNER -> true
                            CalendarContract.Calendars.CAL_ACCESS_ROOT -> true
                            else -> false
                        }

                ret.add(CalendarRecord(
                        calendarId = calID ?: -1L,
                        owner = ownerAccount ?: "",
                        accountName = accountName ?: "",
                        accountType = accountType ?: "",
                        displayName = displayName ?: "",
                        name = name ?: "",
                        color = color ?:0x7f0000ff,
                        isPrimary = (isPrimary ?: 0) != 0,
                        isReadOnly = !isEditable,
                        isVisible = (visible ?: 0) != 0,
                        isSynced = (syncEvents ?: 0) != 0,
                        timeZone = timeZone ?: TimeZone.getDefault().getID()
                ))
            }

            cursor?.close()

        }
        catch (ex: SecurityException) {
        }

        return ret
    }
}
