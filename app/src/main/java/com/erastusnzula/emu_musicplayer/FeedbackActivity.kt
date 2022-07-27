package com.erastusnzula.emu_musicplayer

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import java.util.*
import javax.mail.*
import javax.mail.internet.*
import kotlinx.coroutines.*

class FeedbackActivity : AppCompatActivity() {
    private lateinit var feedbackConstrainedLayout: ConstraintLayout
    private lateinit var subject: EditText
    private lateinit var senderEmailAddress: EditText
    private lateinit var message: EditText
    private lateinit var sendButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_feedback)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Feedback"
        feedbackConstrainedLayout = findViewById(R.id.feedbackConstrainedLayout)
        subject = findViewById(R.id.emailSubject)
        message = findViewById(R.id.emailMessage)
        sendButton = findViewById(R.id.emailSend)
        senderEmailAddress = findViewById(R.id.emailAddress)
        sendButton.setOnClickListener {
            val message =
                message.text.toString() + "\n\nEmail address: " + senderEmailAddress.text.toString()
            val emailSubject = subject.text.toString().trim()
            val username = "nzulaerastus@gmail.com".trim()
            val password = EmailPassword().password
            if (checkForInternetConnection(this) && message.isNotEmpty() && emailSubject.isNotEmpty()) {
                GlobalScope.launch {

                    val properties = Properties()
                    properties["mail.smtp.auth"] = "true"
                    properties["mail.smtp.starttls.enable"] = "true"
                    properties["mail.smtp.host"] = "smtp.gmail.com"
                    properties["mail.smtp.port"] = "587"
                    val session = Session.getInstance(properties, object : Authenticator() {
                        override fun getPasswordAuthentication(): PasswordAuthentication {
                            return PasswordAuthentication(username, password)
                        }
                    })
                    val mail = MimeMessage(session)
                    mail.subject = emailSubject
                    mail.setText(message)
                    mail.setFrom(InternetAddress(username))
                    mail.setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(username)
                    )
                    Transport.send(mail)
                }
                Toast.makeText(
                    this,
                    "Thanks for the feedback.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Subject and message can not be empty and you must have an active network connection.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkForInternetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected

        }
    }

    private fun sendUsingIntent(emailSubject: String, message: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.data = Uri.parse("mailto")
        intent.type = "message/rfc822"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("nzulaerastus@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
        intent.putExtra(Intent.EXTRA_TEXT, message)
        try {
            startActivity(Intent.createChooser(intent, "Choose email host"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
        }
        finish()
    }


}