package com.example.photosharedemo

import android.app.Activity.INPUT_METHOD_SERVICE
import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.photosharedemo.databinding.FragmentShareBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class ShareFragment : Fragment() {

    private lateinit var binding: FragmentShareBinding
    private lateinit var viewModel: MyViewModel
    val authResult =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) {result ->
            if (result.resultCode == RESULT_OK)
                Log.d("Share Fragment", "login success")
            else
                Log.d("Share Fragment", "login fail")
        }
    // handle the image pickup
    var photoUrl: String? = null
    val imagePickUpResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUrl = uri.toString()
    }
    // handle the description input
    var description: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = FragmentShareBinding.inflate(inflater, container, false)
        return binding.root
    }

    // called after the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get the viewModel object associated to this fragment only
        viewModel = ViewModelProvider(this).get(MyViewModel::class.java)

        // monitor the loginState LiveData
        viewModel.authenticationState.observe(viewLifecycleOwner) { state ->
            if (state != MyViewModel.AuthenticationState.AUTHENTICATED)
                launchSignIn()
        }

        // tap the photo pickup button
        binding.selectButton.setOnClickListener {
            imagePickUpResult.launch("image/*")
        }

        // tap the message upload button
        binding.sendButton.setOnClickListener {
            description = binding.editDescription.text.toString()  // description input
            // check the all input fields
            if (checkMessage()) {
                // upload the message to the firestore
                viewModel.uploadMessage(photoUrl, description)
                // reset all input
                photoUrl = null
                description = null
                binding.editDescription.setText("")
                // hide the keyboard
                hideKeyboard()
            }
        }

        // configure the recyclerView
        // set the adapter and the data source from the firestore
        val query = viewModel.collectionRef.orderBy("timestamp")
        val options = FirestoreRecyclerOptions.Builder<PostedMessage>()
            .setQuery(query, PostedMessage::class.java)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()
        val adapter = MessageAdapter(options) { message ->
            // lambda for item selection
            val action = ShareFragmentDirections.actionShareFragmentToShowFragment(
                message.text!!,
                message.timestamp!!.toDate().toString(), message.photoUrl!!)
            findNavController().navigate(action)
        }
        val manager = LinearLayoutManager(activity)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = manager
        binding.recyclerView.addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
        // solve the crash problem after pickuping an image
        binding.recyclerView.itemAnimator = null

        // setup option menu
        setupMenu()

    }

    private fun hideKeyboard() {
        val inputMethodManager = requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }

    private fun checkMessage(): Boolean {
        return if (photoUrl.isNullOrEmpty() || description.isNullOrEmpty()) {
            Toast.makeText(
                context,
                "Select one photo and Input the description",
                Toast.LENGTH_SHORT
            )
                .show()
            false
        } else
            true
    }

    private fun launchSignIn() {
        // set the authentication providers
        val providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build()  // Email/Password Authentication
        )

        // create an intent to launch sign-in activity
        val loginIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        // launch the intent and wait for the result
        authResult.launch(loginIntent)
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.logout -> AuthUI.getInstance().signOut(requireContext())
                }
                return true
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


}