package nl.utwente.ewi.scs.secretnotestaker.secretnotestakerultra

import android.graphics.Bitmap

class UpdateImageRequest(val bitmap: Bitmap, val activity: TakesNotesActivity) : Runnable {
    override fun run() {
        activity.updateImage(bitmap)
    }
}