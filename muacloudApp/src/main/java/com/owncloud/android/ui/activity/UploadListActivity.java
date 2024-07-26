
package com.owncloud.android.ui.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import androidx.fragment.app.FragmentTransaction;
import com.owncloud.android.R;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.operations.CheckCurrentCredentialsOperation;
import com.owncloud.android.presentation.authentication.AccountUtils;
import com.owncloud.android.presentation.transfers.TransferListFragment;
import com.owncloud.android.presentation.transfers.TransfersViewModel;
import com.owncloud.android.utils.MimetypeIconUtil;
import kotlin.Lazy;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

import java.io.File;

import static org.koin.java.KoinJavaComponent.inject;


public class UploadListActivity extends FileActivity {

    private static final String TAG_UPLOAD_LIST_FRAGMENT = "UPLOAD_LIST_FRAGMENT";

    @NotNull Lazy<TransfersViewModel> transfersViewModelLazy = inject(TransfersViewModel.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        View rightFragmentContainer = findViewById(R.id.right_fragment_container);
        rightFragmentContainer.setVisibility(View.GONE);



        setFile(null);

        setupRootToolbar(getString(R.string.uploads_view_title), false, false);

        setupDrawer();

        setupNavigationBottomBar(R.id.nav_uploads);

        if (savedInstanceState == null) {
            createUploadListFragment();
        } // else, the Fragment Manager makes the job on configuration changes
    }

    private void createUploadListFragment() {

        TransferListFragment uploadList = new TransferListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.left_fragment_container, uploadList, TAG_UPLOAD_LIST_FRAGMENT);
        transaction.commit();
    }


    private void openFileWithDefault(String localPath) {
        Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW);
        File file = new File(localPath);
        String mimetype = MimetypeIconUtil.getBestMimeTypeByFilename(localPath);
        if ("application/octet-stream".equals(mimetype)) {
            mimetype = "*/*";
        }
        myIntent.setDataAndType(Uri.fromFile(file), mimetype);
        try {
            startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            showSnackMessage(
                    getString(R.string.file_list_no_app_for_file_type)
            );
            Timber.i("Could not find app for sending log history.");

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FileActivity.REQUEST_CODE__UPDATE_CREDENTIALS && resultCode == RESULT_OK) {

            Account account = AccountUtils.getOwnCloudAccountByName(
                    this,
                    data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            );
            if (account == null) {
                return;
            }
            transfersViewModelLazy.getValue().retryUploadsForAccount(account.name);
        }
    }


    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        if (operation instanceof CheckCurrentCredentialsOperation) {

            getFileOperationsHelper().setOpIdWaitingFor(Long.MAX_VALUE);
            dismissLoadingDialog();
            Account account = ((RemoteOperationResult<Account>) result).getData();
            if (!result.isSuccess()) {

                requestCredentialsUpdate();

            } else {

                transfersViewModelLazy.getValue().retryUploadsForAccount(account.name);
            }

        } else {
            super.onRemoteOperationFinish(operation, result);
        }
    }


    @Override
    protected void onAccountSet(boolean stateWasRecovered) {
        super.onAccountSet(stateWasRecovered);
        if (mAccountWasSet) {
            setAccountInDrawer(getAccount());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
