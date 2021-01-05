package com.simform.videoimageeditor.processActivity

import android.view.View
import android.widget.Toast
import com.arthenica.mobileffmpeg.LogMessage
import com.jaiselrahman.filepicker.model.MediaFile
import com.simform.videoimageeditor.BaseActivity
import com.simform.videoimageeditor.R
import com.simform.videoimageeditor.utils.Common
import com.simform.videoimageeditor.utils.FFmpegCallBack
import com.simform.videoimageeditor.utils.FFmpegQueryExtension
import java.util.concurrent.CyclicBarrier
import kotlinx.android.synthetic.main.activity_motion.btnMotion
import kotlinx.android.synthetic.main.activity_motion.btnVideoPath
import kotlinx.android.synthetic.main.activity_motion.mProgressView
import kotlinx.android.synthetic.main.activity_motion.motionType
import kotlinx.android.synthetic.main.activity_motion.tvInputPathVideo
import kotlinx.android.synthetic.main.activity_motion.tvOutputPath

class MotionActivity : BaseActivity(R.layout.activity_motion) {
    private var isInputVideoSelected: Boolean = false
    override fun initialization() {
        btnVideoPath.setOnClickListener(this)
        btnMotion.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnVideoPath -> {
                Common.selectFile(this, maxSelection = 1, isImageSelection = false, isAudioSelection = false)
            }
            R.id.btnMotion -> {
                when {
                    !isInputVideoSelected -> {
                        Toast.makeText(this, getString(R.string.input_video_validate_message), Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        processStart()
                        val gate = CyclicBarrier(2)
                        val imageToVideo = object : Thread() {
                            override fun run() {
                                gate.await()
                                motionProcess()
                            }
                        }
                        imageToVideo.start()
                        gate.await()
                    }
                }
            }
        }
    }

    private fun motionProcess() {
        val outputPath = Common.getFilePath(this, Common.VIDEO)
        var setpts = 0.5
        var atempo = 2.0
        if (!motionType.isChecked) {
            setpts = 0.5
            atempo = 2.0
        }
        val query = FFmpegQueryExtension.videoMotion(tvInputPathVideo.text.toString(), outputPath, setpts, atempo)
        Common.callQuery(this, query, object : FFmpegCallBack {
            override fun process(logMessage: LogMessage) {
                tvOutputPath.text = logMessage.text
            }

            override fun success() {
                tvOutputPath.text = "Output Path : \n$outputPath"
                processStop()
            }

            override fun cancel() {
                processStop()
            }

            override fun failed() {
                processStop()
            }
        })
    }

    override fun selectedFiles(mediaFiles: List<MediaFile>?, requestCode: Int) {
        when (requestCode) {
            Common.VIDEO_FILE_REQUEST_CODE -> {
                if (mediaFiles != null && mediaFiles.isNotEmpty()) {
                    tvInputPathVideo.text = mediaFiles[0].path
                    isInputVideoSelected = true
                } else {
                    Toast.makeText(this, getString(R.string.video_not_selected_toast_message), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processStop() {
        runOnUiThread {
            btnVideoPath.isEnabled = true
            btnMotion.isEnabled = true
            mProgressView.visibility = View.GONE
        }
    }

    private fun processStart() {
        btnVideoPath.isEnabled = false
        btnMotion.isEnabled = false
        mProgressView.visibility = View.VISIBLE
    }
}