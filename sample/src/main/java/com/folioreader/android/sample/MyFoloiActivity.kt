package com.folioreader.android.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.folioreader.ui.activity.FolioActivity
import com.folioreader.ui.view.DirectionalViewpager
import kotlinx.android.synthetic.main.view.*


class MyFoloiActivity : FolioActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val folioPageViewPager: DirectionalViewpager = findViewById(R.id.folioPageViewPager)
        val param: ConstraintLayout.LayoutParams =
            folioPageViewPager.layoutParams as ConstraintLayout.LayoutParams

        param.setMargins(0, 0, 0, 160)

        val button = Button(this)
        button.id = View.generateViewId()

        val params = ConstraintLayout.LayoutParams(250, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        params.topToBottom = R.id.folioPageViewPager
        params.setMargins(15, 15, 15, 15)

        button.setLayoutParams(params)
        button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER)

        val main: ConstraintLayout = findViewById(R.id.main)
        val headerView = View.inflate(this, R.layout.view, null)
        headerView.id = View.generateViewId()

        main.addView(headerView)

        // Create ConstraintSet
        val constraintSet = ConstraintSet()
        // Make sure all previous Constraints from ConstraintLayout are not lost
        constraintSet.clone(main)

        // Create Rule that states that the START of btn_contact_us1 will be positioned at the END of btn_contact_us2
        constraintSet.connect(
            headerView.getId(),
            ConstraintSet.TOP,
            folioPageViewPager.id,
            ConstraintSet.BOTTOM
        );
        constraintSet.applyTo(main)

        ivDownloadBook.setOnClickListener {
            Log.e("eeerrr", "agdvshvds")
            Toast.makeText(this, "Yaaaa", Toast.LENGTH_SHORT)
        }

//        button.bringToFront()
    }
}