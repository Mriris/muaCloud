

package com.owncloud.android.operations;

import android.accounts.Account;

import com.owncloud.android.domain.files.model.OCFile;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CheckPathExistenceRemoteOperation;
import com.owncloud.android.operations.common.SyncOperation;


public class CheckCurrentCredentialsOperation extends SyncOperation<Account> {

    private Account mAccount;

    public CheckCurrentCredentialsOperation(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("NULL account");
        }
        mAccount = account;
    }

    @Override
    protected RemoteOperationResult<Account> run(OwnCloudClient client) {
        if (!getStorageManager().getAccount().name.equals(mAccount.name)) {
            return new RemoteOperationResult<>(new IllegalStateException(
                    "Account to validate is not the account connected to!"));
        } else {
            RemoteOperation checkPathExistenceOperation = new CheckPathExistenceRemoteOperation(OCFile.ROOT_PATH, false, null);
            final RemoteOperationResult existenceCheckResult = checkPathExistenceOperation.execute(client);
            final RemoteOperationResult<Account> result
                    = new RemoteOperationResult<>(existenceCheckResult.getCode());
            result.setData(mAccount);
            return result;
        }
    }
}
