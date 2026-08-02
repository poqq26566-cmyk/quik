/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.octoshrimpy.quik.interactor

import dev.octoshrimpy.quik.repository.ConversationRepository
import dev.octoshrimpy.quik.repository.MessageRepository
import dev.octoshrimpy.quik.util.Preferences
import io.reactivex.Flowable
import timber.log.Timber
import javax.inject.Inject

class DeleteOldMessages @Inject constructor(
    private val conversationRepo: ConversationRepository,
    private val messageRepo: MessageRepository,
    private val prefs: Preferences
) : Interactor<Unit>() {

    companion object {
        // 自动删除信息页面里两个分类固定用 7 天，跟界面文案（"超过 7 天"）保持一致
        const val CATEGORY_MAX_AGE_DAYS = 7
    }

    override fun buildObservable(params: Unit): Flowable<*> = Flowable.fromCallable {
        val maxAge = prefs.autoDelete.get().takeIf { it > 0 }
        if (maxAge != null) {
            val counts = messageRepo.getOldMessageCounts(maxAge)
            Timber.d("Deleting ${counts.values.sum()} old messages from ${counts.keys.size} conversations")
            messageRepo.deleteOldMessages(maxAge)
            conversationRepo.updateConversations(counts.keys)
        }

        if (prefs.autoDeleteNotifications.get()) {
            val counts = messageRepo.getOldNotificationCounts(CATEGORY_MAX_AGE_DAYS)
            Timber.d("Deleting ${counts.values.sum()} old notification messages from ${counts.keys.size} conversations")
            messageRepo.deleteOldNotifications(CATEGORY_MAX_AGE_DAYS)
            conversationRepo.updateConversations(counts.keys)
        }

        if (prefs.autoDeleteVerificationCodes.get()) {
            val counts = messageRepo.getOldVerificationCodeCounts(CATEGORY_MAX_AGE_DAYS)
            Timber.d("Deleting ${counts.values.sum()} old verification code messages from ${counts.keys.size} conversations")
            messageRepo.deleteOldVerificationCodes(CATEGORY_MAX_AGE_DAYS)
            conversationRepo.updateConversations(counts.keys)
        }
    }

}
