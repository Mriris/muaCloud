

package com.owncloud.android.ui.preview;

import android.content.Context;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.ExoPlaybackException;
import androidx.media3.exoplayer.source.UnrecognizedInputFormatException;
import com.owncloud.android.R;

import java.io.EOFException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;


@OptIn(markerClass = UnstableApi.class)
public class PreviewVideoErrorAdapter {

    private static final int NOT_FOUND_ERROR = 404;
    private static final int TEMPORARY_REDIRECTION = 302;

    
    public static PreviewVideoError handlePreviewVideoError(ExoPlaybackException error,
                                                            Context context) {
        switch (error.type) {
            case ExoPlaybackException.TYPE_SOURCE:
                return handlePlayerSourceError(error, context);
            case ExoPlaybackException.TYPE_UNEXPECTED:
                return handlePlayerError(error.getUnexpectedException().getMessage(), context);
            case ExoPlaybackException.TYPE_RENDERER:
                return handlePlayerError(error.getRendererException().getMessage(), context);
            default:
                // this error will not appear, however it's now covered anyway
                return handlePlayerError("Unknown Exoplayer error", context);
        }
    }

    
    private static PreviewVideoError handlePlayerSourceError(ExoPlaybackException error,
                                                             Context context) {

        //PreviewVideoError previewVideoError;
        final IOException sourceException = error.getSourceException();
        final Throwable cause = sourceException.getCause();

        if (cause != null) {
            if (cause.getCause() instanceof CertificateException) {
                return new PreviewVideoError(
                        context.getString(R.string.streaming_certificate_error), true, false);
            }

            // Cannot connect with the server
            if (sourceException.getCause() instanceof UnknownHostException) {
                return new PreviewVideoError(
                        context.getString(R.string.network_error_socket_exception), false, false);
            }

            // trying to access to a part of the video not available now;
            // ALSO: error obtained when the session expired while playing the video. To handle
            // this case, the parent folder is refreshed and login view is shown
            if (sourceException.getCause() != null &&
                    sourceException.getCause() instanceof EOFException) {
                return new PreviewVideoError(
                        context.getString(R.string.streaming_position_not_available), false, true);
            }
        }

        // Unsupported video file format
        // Important: this error is also thrown when the session is expired an OC server
        // redirects to the IDP.
        // To handle this case, the parent folder is refreshed and login view is shown
        if (sourceException instanceof UnrecognizedInputFormatException) {
            return new PreviewVideoError(
                    context.getString(R.string.streaming_unrecognized_input), true, true);
        }

        if (sourceException instanceof HttpDataSource.InvalidResponseCodeException) {

            // Video file no longer exists in the server
            if (((HttpDataSource.InvalidResponseCodeException) sourceException).responseCode == NOT_FOUND_ERROR) {
                return new PreviewVideoError(
                        context.getString(R.string.streaming_file_not_found_error), false, false);
            }

            // redirections are allowed, but crossed redirections not
            if ((((HttpDataSource.InvalidResponseCodeException) sourceException).responseCode == TEMPORARY_REDIRECTION)) {
                return new PreviewVideoError(
                        context.getString(R.string.streaming_crossed_redirection), true, false);
            }
        }

        // if error could not be detected properly
        return new PreviewVideoError(context.getString(R.string.previewing_video_common_error), true, false);
    }

    
    private static PreviewVideoError handlePlayerError(final String errorMessage,
                                                       Context context) {
        return (errorMessage != null)
                ? new PreviewVideoError(errorMessage, false, false)
                : new PreviewVideoError(
                context.getString(R.string.previewing_video_common_error), false, false);
    }

}
