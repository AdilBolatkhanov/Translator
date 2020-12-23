package com.example.translatorkotlin.fragments.account

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.example.common.data.User
import com.example.translatorkotlin.MainActivity
import com.example.translatorkotlin.R
import com.example.translatorkotlin.adapters.StatisticsViewPagerAdapter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_account.*
import java.util.concurrent.TimeUnit

private const val GOOGLE_SIGN_IN = 9001
const val EMAIL = "email"
const val USER_ID = "uid"
const val NAME = "name"
const val URL_ACC = "url"
const val TIME = "time"
const val AUTH = "auth"
const val QUANTITY = "quantity"
const val STARTTIME = "start"

class AccountFragment : Fragment(R.layout.fragment_account) {
    private var firebaseAuth: FirebaseAuth? = null
    private var sharedPreferences: SharedPreferences? = null
    private val database = Firebase.database

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialSetup()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        checkAuthorization()
        readFromFBDB()
        setListeners()
    }

    private fun setListeners() {
        signInButton.setOnClickListener {
            signIn()
        }

        exitBtn.setOnClickListener {
            firebaseAuth?.signOut()
            sharedPreferences!!.edit().putBoolean(AUTH, false).apply()
            changeVisibilityAuthorized(false)
        }
    }

    private fun checkAuthorization() {
        if (sharedPreferences!!.getBoolean(AUTH, false)) {
            changeVisibilityAuthorized(true)

            setViewsWithValues()
            setTime()
            setupTabLayout()
        }
    }

    private fun setViewsWithValues() {
        emailTextView.text = sharedPreferences!!.getString(EMAIL, "")
        nameTextView.text = sharedPreferences!!.getString(NAME, "")
        val url = sharedPreferences!!.getString(URL_ACC, "")
        Glide.with(requireContext()).load(url).into(userImage)
    }

    private fun changeVisibilityAuthorized(authorized: Boolean) {
        imageTurtle.isVisible = !authorized
        autorithationTextView.isVisible = !authorized
        signInButton.isVisible = !authorized
        userImage.isVisible = authorized
        cardView.isVisible = authorized
        emailTextView.isVisible = authorized
        nameTextView.isVisible = authorized
        exitBtn.isVisible = authorized
        tabStatistics.isVisible = authorized
        viewPager.isVisible = authorized
    }

    private fun setTime() {
        val start =
            sharedPreferences!!.getString(STARTTIME, "0")!!.toLong()
        val end = System.currentTimeMillis()
        val total = end - start
        val minutes = TimeUnit.MILLISECONDS.toMinutes(total)
        val totalTime =
            sharedPreferences!!.getString(TIME, "0")!!.toLong() + minutes
        (activity as MainActivity).writeToFBDB(
            sharedPreferences!!.getString(USER_ID, "")!!,
            sharedPreferences!!.getString(NAME, "")!!,
            sharedPreferences!!.getString(EMAIL, "")!!,
            sharedPreferences!!.getString(URL_ACC, "")!!,
            "${sharedPreferences!!.getInt(QUANTITY, 0)}",
            "$totalTime"
        )
        val newStart = System.currentTimeMillis()
        sharedPreferences!!.edit().putString(TIME, "$totalTime")
            .putString(STARTTIME, "$newStart").apply()
    }

    private fun initialSetup() {
        (activity as AppCompatActivity).findViewById<TextView>(R.id.toolbarText).text = "Account"
        (activity as AppCompatActivity).findViewById<ImageView>(R.id.overflowToolbar)
            .setImageResource(R.drawable.ic_overflow)
        val textView = (activity as AppCompatActivity).findViewById<TextView>(R.id.exchangeTextView)
        (activity as AppCompatActivity).findViewById<Toolbar>(R.id.toolbar).removeView(textView)
        (activity as MainActivity).setPopupMenu()
    }

    private fun readFromFBDB() {
        val myRef: DatabaseReference = database.getReference("users")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.value != null && dataSnapshot.hasChildren()) {
                    val userArrayList = mutableListOf<User>()
                    for (child in dataSnapshot.children) {
                        val user: User? = child.getValue<User>()
                        userArrayList.add(user!!)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("AccountFragment", databaseError.message)
            }
        })
    }

    private fun signIn() {
        firebaseAuth = FirebaseAuth.getInstance()

        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(this.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(requireContext(), options)
        val signInIntent = googleSignInClient.signInIntent

        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account?.idToken, null)

                firebaseAuth
                    ?.signInWithCredential(credential)
                    ?.addOnCompleteListener { authResult ->
                        changeVisibilityAuthorized(authResult.isSuccessful)
                        handleAuthResult(authResult)
                    }
            } catch (e: ApiException) {
                Log.e(e.toString(), "Google sign in failed")
            }
        }
    }

    private fun handleAuthResult(authResult: Task<AuthResult>) {
        if (authResult.isSuccessful) {
            firebaseAuth?.currentUser?.let { user ->
                user.getIdToken(true).addOnCompleteListener { result ->
                    result.result?.token
                    updateUserInfo(user)
                    getValuesOfExistingUsers(user)

                    updateValuesOfUsersToDb(user)
                    setupTabLayout()
                }
            }
        } else {
            Log.e("SigIn", "Failure signInWithCredential")
        }
    }

    private fun updateValuesOfUsersToDb(user: FirebaseUser) {
        val quantity = sharedPreferences!!.getInt(QUANTITY, 0)
        val time = sharedPreferences!!.getString(TIME, "0")

        val start: Long = System.currentTimeMillis()
        sharedPreferences!!.edit()
            .putString(STARTTIME, "$start")
            .apply()

        (activity as MainActivity).writeToFBDB(
            user.uid,
            user.displayName!!,
            user.email!!,
            user.photoUrl.toString(),
            "$quantity",
            time!!
        )
    }

    private fun getValuesOfExistingUsers(user: FirebaseUser) {
        if (!sharedPreferences!!.contains(QUANTITY) ||
            !sharedPreferences!!.contains(TIME)
        ) {
            val myRef = database.getReference("users").child(user.uid)
            myRef.addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.e("AccountFragment", error.message)
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value != null) {
                        val userFromDb = snapshot.getValue<User>()
                        val count: String? = userFromDb!!.countWords
                        val time: String? = userFromDb.time
                        sharedPreferences!!.edit()
                            .putInt(QUANTITY, count!!.toInt())
                            .putString(TIME, time).apply()
                    }
                }
            })
        }
    }

    private fun setupTabLayout() {
        val adapter = StatisticsViewPagerAdapter(activity as MainActivity)
        viewPager.adapter = adapter
        TabLayoutMediator(tabStatistics, viewPager) { tab, position ->
            tab.text = adapter.getTitle(position)
        }.attach()
    }

    private fun updateUserInfo(user: FirebaseUser) {
        nameTextView.text = user.displayName
        emailTextView.text = user.email
        Glide.with(requireContext()).load(user.photoUrl)
            .into(userImage)
        sharedPreferences!!.edit().putString(NAME, user.displayName)
            .putString(EMAIL, user.email)
            .putString(USER_ID, user.uid)
            .putString(URL_ACC, user.photoUrl.toString())
            .putBoolean(AUTH, true)
            .apply()
    }

}
