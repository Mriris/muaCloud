

package com.owncloud.android.presentation.sharing.sharees

import android.accounts.Account
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.owncloud.android.R
import com.owncloud.android.databinding.SearchUsersGroupsLayoutBinding
import com.owncloud.android.domain.files.model.OCFile
import com.owncloud.android.domain.sharing.shares.model.OCShare
import com.owncloud.android.domain.sharing.shares.model.ShareType
import com.owncloud.android.domain.utils.Event
import com.owncloud.android.presentation.common.UIResult
import com.owncloud.android.presentation.sharing.ShareFragmentListener
import com.owncloud.android.presentation.sharing.ShareViewModel
import com.owncloud.android.utils.PreferenceUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber


class SearchShareesFragment : Fragment(),
    ShareUserListAdapter.ShareUserAdapterListener {

    // Parameters
    private var file: OCFile? = null
    private var account: Account? = null

    // other members
    private var userGroupsAdapter: ShareUserListAdapter? = null
    private var listener: ShareFragmentListener? = null

    private val shareViewModel: ShareViewModel by viewModel {
        parametersOf(
            file?.remotePath,
            account?.name
        )
    }

    private var _binding: SearchUsersGroupsLayoutBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            file = it.getParcelable(ARG_FILE)
            account = it.getParcelable(ARG_ACCOUNT)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SearchUsersGroupsLayoutBinding.inflate(inflater, container, false)
        return binding.root.apply {
            // Allow or disallow touches with other visible windows
            filterTouchesWhenObscured = PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(context)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get the SearchView and set the searchable configuration
        val searchView = view.findViewById<SearchView>(R.id.searchView)
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(
            searchManager.getSearchableInfo(
                requireActivity().componentName
            )   // assumes parent activity is the searchable activity
        )
        searchView.setIconifiedByDefault(false)    // do not iconify the widget; expand it by default

        searchView.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI // avoid fullscreen with softkeyboard

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Timber.v("onQueryTextSubmit intercepted, query: $query")
                return true    // return true to prevent the query is processed to be queried;
                // a user / group will be picked only if selected in the list of suggestions
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false   // let it for the parent listener in the hierarchy / default behaviour
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().setTitle(R.string.share_with_title)

        // Load private shares in the list
        observePrivateShares()
    }

    private fun observePrivateShares() {
        shareViewModel.shares.observe(
            viewLifecycleOwner,
            Event.EventObserver { uiResult ->
                val privateShares = uiResult.getStoredData()?.filter { share ->
                    share.shareType == ShareType.USER ||
                            share.shareType == ShareType.GROUP ||
                            share.shareType == ShareType.FEDERATED
                }
                when (uiResult) {
                    is UIResult.Success -> {
                        privateShares?.let {
                            updatePrivateShares(privateShares)
                        }
                        listener?.dismissLoading()
                    }
                    is UIResult.Error -> {}
                    is UIResult.Loading -> {}
                }
            }
        )
    }

    private fun updatePrivateShares(privateShares: List<OCShare>) {
        // Update list of users/groups
        userGroupsAdapter = ShareUserListAdapter(
            requireActivity().applicationContext,
            R.layout.share_user_item, privateShares, this
        )

        // Show data
        if (privateShares.isNotEmpty()) {
            binding.searchUsersListView.isVisible = true
            binding.searchUsersListView.adapter = userGroupsAdapter
        } else {
            binding.searchUsersListView.isVisible = false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = activity as ShareFragmentListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(requireActivity().toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onStart() {
        super.onStart()
        // focus the search view and request the software keyboard be shown
        if (binding.searchView.requestFocus()) {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchView.findFocus(), InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideSoftKeyboard()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun hideSoftKeyboard() {
        view?.let {
            view?.findViewById<View>(R.id.searchView)?.let {
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
            }
        }
    }

    override fun unshareButtonPressed(share: OCShare) {
        Timber.d("Removed private share with ${share.sharedWithDisplayName}")
        listener?.showRemoveShare(share)
    }

    override fun editShare(share: OCShare) {
        // move to fragment to edit share
        Timber.d("Editing ${share.sharedWithDisplayName}")
        listener?.showEditPrivateShare(share)
    }

    companion object {
        // the fragment initialization parameters
        private const val ARG_FILE = "FILE"
        private const val ARG_ACCOUNT = "ACCOUNT"


        fun newInstance(fileToShare: OCFile, account: Account) = SearchShareesFragment().apply {
            arguments = bundleOf(
                ARG_FILE to fileToShare,
                ARG_ACCOUNT to account
            )
        }
    }
}
