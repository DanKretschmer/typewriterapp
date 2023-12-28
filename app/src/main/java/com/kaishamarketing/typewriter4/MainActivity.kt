package com.kaishamarketing.typewriter4

import com.kaishamarketing.typewriter4.R
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import android.util.Log
import android.widget.Toast
import android.text.Editable
import android.text.TextWatcher
import android.content.Context
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import android.view.View
import android.view.KeyEvent
import android.media.MediaPlayer
import android.view.inputmethod.InputMethodManager
import android.inputmethodservice.KeyboardView;
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException




class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)





        val mainLayout = findViewById<View>(R.id.main_layout) // Replace with the ID of your main layout

        mainLayout.setOnLongClickListener {
            showOptionsDialog()
            true // Indicates that the long click was handled
        }


        val typeface = ResourcesCompat.getFont(this, R.font.xtypewriter) // Ensure the font resource is correctly referenced
       // val textView = findViewById<TextView>(R.id.textView)

        val editText = findViewById<EditText>(R.id.editText)

        editText.requestFocus()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)



        editText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                Log.d("MainActivity", "EditText has focus")
            } else {
                Log.d("MainActivity", "EditText lost focus")
            }
        }




        editText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_ENTER -> playSound(R.raw.carriage)
                    KeyEvent.KEYCODE_SPACE -> playSound(R.raw.space)
                    else -> {
                        // Check if the typed character is a letter, digit, or symbol
                        val typedChar = event.unicodeChar.toChar()
                        if (typedChar.isLetterOrDigit() || typedChar.isWhitespace()) {
                            playRandomTypewriterSound()
                        }
                    }
                }
            }
            false
        }


        //val textView: TextView = findViewById(R.id.correctTextViewId)
     //   textView.typeface = typeface

        editText.addTextChangedListener(object : TextWatcher {
            private var oldText = ""

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Code here
                oldText = s.toString() // Store the old text
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Code here

                // Check for specific keys like space, letters, or punctuation
                if (s.length > oldText.length) {
                    val lastTypedChar = s[s.length - 1]
                    when (lastTypedChar) {
                        ' ' -> playSound(R.raw.space)
                        '\n' -> playSound(R.raw.carriage)
                        in 'a'..'z', in 'A'..'Z', in '0'..'9' -> playRandomTypewriterSound()
                        // Add more cases for other characters if needed
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                // Code here
                if (s.length < oldText.length) {
                    // Revert to old text
                    editText.removeTextChangedListener(this) // Temporarily remove listener to prevent infinite loop
                    editText.setText(oldText)
                    editText.setSelection(oldText.length) // Set cursor position to the end of the text
                    editText.addTextChangedListener(this) // Re-attach the listener
                }
            }
        })




//        editText.addTextChangedListener(object : TextWatcher {
//            private var oldText = ""
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//                // Code here
//                oldText = s.toString() // Store the old text
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                // Code here
//
//            }
//
//            override fun afterTextChanged(s: Editable) {
//                // Code here
//                if (s.length < oldText.length) {
//                    // Revert to old text
//                    editText.removeTextChangedListener(this) // Temporarily remove listener to prevent infinite loop
//                    editText.setText(oldText)
//                    editText.setSelection(oldText.length) // Set cursor position to the end of the text
//                    editText.addTextChangedListener(this) // Re-attach the listener
//                }
//            }
//        })
//
   }

    fun onFullWidthButtonClick(view: View) {
        // Perform the desired action when the button is clicked
        // For example, show a dialog or perform some other task
        Log.d("Button Click", "PDF button clicked")


        showOptionsDialog()
    }

    private fun showOptionsDialog() {
        val editText = findViewById<EditText>(R.id.editText)
        val text = editText.text.toString() // Get text from EditText
        val options = arrayOf("Print to PDF", "Save to Text File")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose an action")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> generatePdf(text)
                1 -> saveToTextFile(text)
            }
        }
        builder.show()
    }
    private fun saveToTextFile(text: String) {
        // Code to save text to a file
    }

    private fun generatePdf(text: String) {
        // Check for write permission
        Log.d("generate pdf", "generate pdf started")
        Log.d("generatePdf", "WRITE_REQUEST_CODE: $WRITE_REQUEST_CODE")
        val editText = findViewById<EditText>(R.id.editText)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request for permission
            Log.d("generatePdf", "Permission not granted, requesting...")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_REQUEST_CODE
            )
        } else {
            // Permission is granted, proceed with PDF generation
            Log.d("generatePdf", "Permission granted, generating PDF...")
            createPdf(editText.text.toString())
        }
    }

    private fun createPdf(text: String) {
        Log.d("createPdf", "CreatePDF function started")
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val customTypeface = ResourcesCompat.getFont(this, R.font.xtypewriter)
        val textLines = text.split("\n")
        val canvas = page.canvas
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = customTypeface // Apply the custom font
        canvas.drawText(text, 10f, 50f, paint)  // Draw the text
        // Set the initial y-coordinate for drawing
        var y = 50f

        // Loop through the lines and draw each line on a new y-coordinate
        for (line in textLines) {
            canvas.drawText(line, 10f, y, paint)
            y += paint.descent() - paint.ascent() // Move to the next line
        }



        pdfDocument.finishPage(page)

        // Prepare the file path
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (!directory.exists()) {
            directory.mkdirs() // Create directory if it does not exist
        }
        val file = File(directory, "TypewriterText.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Log.d("PDF", "PDF saved to ${file.absolutePath}")

            // Display a toast message to indicate success
            runOnUiThread {
                Toast.makeText(this, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
            Toast.makeText(this, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("PDF", "Error saving PDF: ${e.message}")
            runOnUiThread {
                Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }


    companion object {
        private const val WRITE_REQUEST_CODE = 101
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val editText = findViewById<EditText>(R.id.editText)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted

                createPdf(editText.text.toString())
            } else {
                // Permission was denied
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun playSound(soundResId: Int) {
        val mediaPlayer = MediaPlayer.create(this, soundResId)
        mediaPlayer.setOnCompletionListener { mp -> mp.release() }
        mediaPlayer.start()
    }

    private fun playRandomTypewriterSound() {
        val soundNumber = (1..5).random() // Random number between 1 and 5
        val soundResId = when (soundNumber) {
            1 -> R.raw.key1
            2 -> R.raw.key2
            3 -> R.raw.key3
            4 -> R.raw.key4
            5 -> R.raw.key5

            else -> R.raw.key1
        }
        playSound(soundResId)
    }




    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            // Check if the pressed key is the Delete key
            if (event.keyCode == KeyEvent.KEYCODE_DEL) {
                // Get the EditText
                val editText = findViewById<EditText>(R.id.editText)
                // Check cursor position
                val cursorPos = editText.selectionStart
                if (cursorPos > 0) {
                    // Allow backspace functionality
                    editText.text.delete(cursorPos - 1, cursorPos)
                    return true
                }
                return true // Prevent the default behavior of the delete key
            }
        }
        return super.dispatchKeyEvent(event)
    }





}