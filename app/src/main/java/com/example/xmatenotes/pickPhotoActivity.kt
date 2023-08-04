package com.example.xmatenotes

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class pickPhotoActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_SELECT_PHOTO = 1
        private const val EXTRA_BITMAP = "extra_bitmap"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pickphoto)
    }

    fun selectPhotoFromGallery(view: View) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_SELECT_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            val selectedImage = data?.data
            selectedImage?.let {
                val bitmap = convertUriToBitmap(selectedImage)

                // 将位图添加为附加数据
                val intent = Intent(this, QRGenerateActivity::class.java)
                if (bitmap != null) {
                    BitmapCacheManager.putBitmap("Bitmap", bitmap)
                }
                startActivity(intent)
            }
        }
    }

    private fun convertUriToBitmap(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "无法加载选中的图片", Toast.LENGTH_SHORT).show()
            null
        }
    }
}
