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
import android.app.PendingIntent
import android.content.Intent
import android.service.notification.StatusBarNotification

fun Notification.getTitleAndText(): Pair<String, String>
{
	val extras = this.extras;

	var title: String = "";
	var text: String = "";

	if (extras != null)
	{
		if ((extras.get(Notification.EXTRA_TITLE) != null || extras.get(Notification.EXTRA_TITLE_BIG) != null)
			&&
			(extras.get(Notification.EXTRA_TEXT) != null || extras.get(Notification.EXTRA_TEXT_LINES) != null))
		{
			if (extras.get(Notification.EXTRA_TITLE_BIG) != null)
			{
				val bigTitle = extras.getCharSequence(Notification.EXTRA_TITLE_BIG) as CharSequence;
				if (bigTitle.length < 40 || extras.get(Notification.EXTRA_TITLE) == null)
					title = bigTitle.toString();
				else
					title = extras.getCharSequence(Notification.EXTRA_TITLE).toString();
			}
			else
				title = extras.getCharSequence(Notification.EXTRA_TITLE).toString();

			if (extras.get(Notification.EXTRA_TEXT_LINES) != null)
			{
				for (line in extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES))
				{
					text += line;
					text += "\n\n";
				}
				text = text.trim();
			}
			else
			{
				text = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
			}
		}
	}

	return Pair(title, text)
}
