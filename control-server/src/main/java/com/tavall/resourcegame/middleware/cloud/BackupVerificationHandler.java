package org.tavall.control.cloud;

import java.util.UUID;

public final class BackupVerificationHandler implements IBackupVerificationHandler, ICloudControlDomain {
    public boolean verify(UUID backupId) {
        BackupJob backupJob = getCloudRepository().findBackupJob(backupId).orElseThrow();
        if (backupJob.status() == BackupStatus.COMPLETED && backupJob.checksum().isPresent()) {
            getCloudRepository().saveBackupJob(backupJob.withVerificationStatus(BackupVerificationStatus.VERIFIED));
            return true;
        }
        getCloudRepository().saveBackupJob(backupJob.withVerificationStatus(BackupVerificationStatus.FAILED));
        return false;
    }
}
