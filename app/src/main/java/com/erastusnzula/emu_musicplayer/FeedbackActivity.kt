package com.erastusnzula.emu_musicplayer

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.util.*
import javax.mail.*
import javax.mail.internet.*

class FeedbackActivity : AppCompatActivity() {
    private lateinit var subject: EditText
    private lateinit var message: EditText
    private lateinit var sendButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_EMUMusicPlayer)
        setContentView(R.layout.activity_feedback)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Feedback"

        subject = findViewById(R.id.emailSubject)
        message = findViewById(R.id.emailMessage)
        sendButton = findViewById(R.id.emailSend)
        sendButton.setOnClickListener {
            val message = message.text.toString().trim()
            val emailSubject = subject.text.toString().trim()
            val username = "nzulaerastus@gmail.com".trim()
            val password = EmailPassword().password
            val network = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (message.isNotEmpty() && emailSubject.isNotEmpty()) {
                if (network.activeNetworkInfo?.isConnectedOrConnecting == true) {
                    Thread {
                        try {
                            val prop = Properties()
                            prop["mail.smtp.auth"] = "true"
                            prop["mail.smtp.starttls.enable"] = "true"
                            prop["mail.smtp.host"] = "smtp.gmail.com"
                            prop["mail.smtp.port"] = "587"
                            val session = Session.getInstance(prop, object : Authenticator() {
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
                        } catch (e: Exception) {
                            Toast.makeText(
                                this,
                                "Failed to send message, try again",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }.start()
                    sentEmailConfirmation()

                }else{
                    Toast.makeText(this, "No active network connection available.", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this, "Subject and message can't be empty.", Toast.LENGTH_LONG).show()
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

    private fun sentEmailConfirmation(){
        val dialog = AlertDialog.Builder(this)
        dialog.setView(LayoutInflater.from(this).inflate(R.layout.feedback_sent, null))
        dialog.setTitle("Feedback")
        dialog.setCancelable(false)
        dialog.setPositiveButton("Okay"){dialogDismiss,_->
            dialogDismiss.dismiss()
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
        dialog.show()
    }
}