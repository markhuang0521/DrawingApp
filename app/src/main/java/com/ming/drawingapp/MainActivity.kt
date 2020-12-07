package com.ming.drawingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(),
    AdapterView.OnItemSelectedListener {

    companion object {
        private val STORAGE_PERMISSION_CODE = 100
        private val PICK_IMAGE_CODE = 1

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val colorList2 = resources.getStringArray(R.array.brush_color).toList()

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.brush_size,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinnerBrushSize = findViewById<Spinner>(R.id.spinnerBrushSize)
        spinnerBrushSize.adapter = adapter
        spinnerBrushSize.onItemSelectedListener = this

//color spinner init
        spinnerColor.adapter = ImageSpinner(this, colorList2)
        spinnerColor.onItemSelectedListener = this

        imageBtnChangeImage.setOnClickListener {
            if (checkPermission()) {
                chooseImageFromGallery()
            }
        }
        imageBtnUndo.setOnClickListener { undoDrawPaint() }
        imageBtnSave.setOnClickListener {
            if (checkPermission()) {

                ImageSaveTask(createBitmapFromView(frameLayout_drawingView)).execute()
            }
        }


    }

    private fun createBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return bitmap
    }

    private fun undoDrawPaint() {
        drawingView.undoDrawPath()
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,

                Manifest.permission.READ_EXTERNAL_STORAGE


            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ).toString()
                )
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSION_CODE
                )
            } else {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
        return false
    }

    private fun chooseImageFromGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_IMAGE_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty() && requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "camera permission granted, opening galley",
                    Toast.LENGTH_SHORT
                ).show()


            } else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show()

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_CODE) {
            val imageUri: Uri? = data?.data
            Glide.with(this).load(imageUri).centerCrop().into(ivPaintBackground)
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // for each spinner
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent!!.id) {
            R.id.spinnerBrushSize -> {

                val size = parent.getItemAtPosition(position).toString()
                Log.d("size3", size)
                if (size != resources.getString(R.string.spinner_size))
                    drawingView.setBrushSize(size.toFloat())
            }
            R.id.spinnerColor -> {
                val color = Color.parseColor(parent.getItemAtPosition(position) as String)
                drawingView.setBrushColor(color)

            }
        }


    }


    @SuppressLint("StaticFieldLeak")
    private inner class ImageSaveTask(val bitmap: Bitmap) : AsyncTask<Any, Void, String>() {
        private lateinit var dialog: Dialog

        private fun showProgressDialog() {
            dialog = Dialog(this@MainActivity)
            dialog.setContentView(R.layout.loading_dialog)
            dialog.show()


        }

        private fun hideProgressDialog() {
            dialog.dismiss()


        }

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()

        }

        override fun doInBackground(vararg p0: Any?): String {

            var result = ""
            if (bitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    val file =
                        File(
                            externalCacheDir!!.absoluteFile.toString()
                                    + File.separator + "DrawingApp_"
                                    + System.currentTimeMillis() / 1000 + ".png"
                        )
                    val fileOutput = FileOutputStream(file)
                    fileOutput.write(bytes.toByteArray())
                    fileOutput.close()
                    result = file.absolutePath

                } catch (e: Exception) {
                    Log.d("error", e.message.toString())

                }
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            hideProgressDialog()

            if (!result!!.isEmpty()) {
                Toast.makeText(this@MainActivity, "file saved", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this@MainActivity, "file failed", Toast.LENGTH_SHORT).show()

            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null) { path, uri ->
                val intent = Intent(Intent.ACTION_SEND)
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.type = "image/png"
                startActivity(Intent.createChooser(intent, "Share Image"))
            }
        }

    }


}
