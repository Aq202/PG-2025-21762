package com.example.public_transport_app.ui.homePage

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CenterLocationButtonController(
    val centerLocationButton: FloatingActionButton,
    private var bottomSheetState: Int = BottomSheetBehavior.STATE_COLLAPSED,
    private var isCameraFollowingUser: Boolean = false
) {

    fun setBottomSheetState(bottomSheetState: Int){
        this.bottomSheetState = bottomSheetState
        updateVisibility()
    }

    fun setIsCameraFollowingUser(isCameraFollowingUser: Boolean){
        this.isCameraFollowingUser = isCameraFollowingUser
        updateVisibility()
    }


    private fun updateVisibility() {
        val shouldShow = bottomSheetState != BottomSheetBehavior.STATE_EXPANDED &&
                !isCameraFollowingUser

        if (shouldShow) {
            centerLocationButton.show()
        } else {
            centerLocationButton.hide()
        }
    }


}