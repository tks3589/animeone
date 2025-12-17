package com.aaron.chen.animeone.extension

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

fun Activity.launchInAppReview() {
    val reviewManager = ReviewManagerFactory.create(this)

    val request = reviewManager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            Log.d("aaron_tt_launchInAppReview", "success")
            val reviewInfo = task.result
            val flow = reviewManager.launchReviewFlow(this, reviewInfo)
            flow.addOnCompleteListener {
                // ⚠️ 注意：這裡不代表使用者真的有評分
                // Google 不會告訴你結果
                Log.d("aaron_tt_launchInAppReview", "addOnCompleteListener")
            }.addOnFailureListener {
                Log.d("aaron_tt_launchInAppReview", "addOnFailureListener")
            }
        } else {
            // 失敗（通常是條件不符 or quota 用完）
            Log.d("aaron_tt_launchInAppReview", "fail")
        }
    }
}