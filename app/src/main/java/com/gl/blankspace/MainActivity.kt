package com.gl.blankspace

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.gl.blankspace.bean.MoveBean
import com.gl.blankspace.bean.PointBean
import com.gl.blankspace.bean.ScaleBean
import com.gl.blankspace.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.rabtman.wsmanager.WsManager
import com.rabtman.wsmanager.listener.WsStatusListener
import com.gl.blankspaceview.widget.BlankPhotoView
import com.gl.blankspaceview.widget.draw.Coordinate
import com.gl.blankspaceview.widget.draw.MarkPath
import com.gl.blankspaceview.widget.draw.MyPathUtils
import com.gl.blankspaceview.widget.draw.PathContainer
import com.gl.blankspaceview.widget.draw.ViewProvider
import com.gl.blankspaceview.widget.photoview.PhotoViewAttacker
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private val dataBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val context by lazy { this }
    private var wsManager: WsManager? = null;
    private var rotate = 0f;
    private val pathList: ArrayList<Coordinate> = ArrayList()
    private val isOperation = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(dataBinding.root)
        dataBinding.menu.root.visibility = if (isOperation) View.VISIBLE else View.INVISIBLE
        Glide.with(context).load(R.mipmap.test).into(dataBinding.photoView)
        dataBinding.photoView.setDrawMode(BlankPhotoView.DrawMode.path)
        //画笔颜色和粗细
        dataBinding.photoView.setPathColor(Color.parseColor("#0000ff"));
        dataBinding.photoView.setPathSize(10)
        dataBinding.photoView.setOperation(isOperation)
        initListener()
        initSocket()
    }

    private fun initListener() {
        dataBinding.menu.radio.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.path -> {
                    dataBinding.photoView.setDrawMode(BlankPhotoView.DrawMode.path)
                }
                R.id.eraser -> {

                    dataBinding.photoView.setDrawMode(BlankPhotoView.DrawMode.eraser)
                }
                else -> {
                    dataBinding.photoView.setDrawMode(BlankPhotoView.DrawMode.normal)
                }
            }
        }

        dataBinding.menu.rotate.setOnClickListener {
            rotate += 90f;
            dataBinding.photoView.rotateTo(rotate)
            if (isOperation) {
                wsManager?.apply {
                    val pointBean = PointBean();
                    pointBean.instructions = MarkPath.ACTION_ROTATE;
                    pointBean.rotate = rotate;
                    val json = Gson().toJson(pointBean)
                    sendMessage(json)
                }
            }

        }

        dataBinding.menu.back.setOnClickListener {
            dataBinding.photoView.revert()
            if (isOperation) {
                wsManager?.apply {
                    val pointBean = PointBean();
                    pointBean.instructions = MarkPath.ACTION_REVERT;
                    val json = Gson().toJson(pointBean)
                    sendMessage(json)
                }
            }

        }

        dataBinding.menu.clear.setOnClickListener {
            dataBinding.photoView.clearPath();
            if (isOperation) {
                wsManager?.apply {
                    val pointBean = PointBean();
                    pointBean.instructions = MarkPath.ACTION_CLEAR;
                    val json = Gson().toJson(pointBean)
                    sendMessage(json)
                }
            }

        }

        dataBinding.photoView.setOnDragChangeListener(object : PhotoViewAttacker.OnDragChangeListener {
            override fun onDragChange(dx: Float, dy: Float, totalDx: Float, totalDy: Float) {
                if (isOperation) {
                    wsManager?.apply {
                        val pointBean = PointBean();
                        pointBean.instructions = MarkPath.ACTION_MOVE;
                        val moveBean = MoveBean();
                        moveBean.dx = MyPathUtils.getXRatio(dx, dataBinding.photoView);
                        moveBean.dy = MyPathUtils.getYRatio(dy, dataBinding.photoView);
                        moveBean.totalDx = MyPathUtils.getXRatio(totalDx, dataBinding.photoView);
                        moveBean.totalDy = MyPathUtils.getYRatio(totalDy, dataBinding.photoView);
                        pointBean.mMoveBean = moveBean;
                        val json = Gson().toJson(pointBean)
                        sendMessage(json)
                    }

                }
            }

            override fun fling(viewWidth: Int, viewHeight: Int, velocityX: Int, velocityY: Int) {

                if (isOperation) {
                    //快速移动监听
                    val viewProvider = object : ViewProvider {
                        override fun getWidth(): Int {
                            return viewWidth;
                        }

                        override fun getHeight(): Int {
                            return viewHeight;
                        }

                        override fun invalidate() {

                        }

                        override fun getResources(): Resources? {
                            return null;
                        }

                    }
                    wsManager?.apply {
                        val pointBean = PointBean();
                        pointBean.instructions = MarkPath.ACTION_MOVE;
                        val moveBean = MoveBean();
                        moveBean.velocityX = MyPathUtils.getXRatio(velocityX.toFloat(), viewProvider).toInt();
                        moveBean.velocityY = MyPathUtils.getYRatio(velocityY.toFloat(), viewProvider).toInt();
                        pointBean.mMoveBean = moveBean;
                        val json = Gson().toJson(pointBean)
                        sendMessage(json)
                    }

                }
            }
        })

        dataBinding.photoView.setOnDrawCompleteListener { action, points ->
            if (isOperation) {
                val pointBean = PointBean();
                pointBean.instructions = action;
                pointBean.pointXs = Coordinate.getXs(points)
                pointBean.pointYs = Coordinate.getYs(points)
                val json = Gson().toJson(pointBean)
                wsManager?.apply {
                    sendMessage(json)
                }
            }

        }

        dataBinding.photoView.setOnZoomChangeListener { scaleFactor, focusX, focusY ->
            if (isOperation) {
                val pointBean = PointBean();
                pointBean.instructions = MarkPath.ACTION_SCALE;
                val scaleBean = ScaleBean();
                scaleBean.scaleFactor = scaleFactor
                scaleBean.focusX = focusX
                scaleBean.focusY = focusY
                pointBean.mScaleBean = scaleBean
                val json = Gson().toJson(pointBean)
                wsManager?.apply {
                    sendMessage(json)
                }
            }
        }

/*        dataBinding.shadeView.setOnCoverChangeLister {
            if (isOperation) {
                val pointBean = PointBean();
                pointBean.instructions = MarkPath.ACTION_SHADE;
                pointBean.shadeHeight = it
                val json = Gson().toJson(pointBean)
                wsManager?.apply {
                    sendMessage(json)
                }
            }
        }*/

    }

    override fun onDestroy() {
        super.onDestroy()
        wsManager?.apply {
            stopConnect()
        }
    }

    private fun initSocket() {
        if (null != wsManager) {
            wsManager!!.stopConnect()
            wsManager = null;
        }
        wsManager = WsManager.Builder(context)
            .client(
                OkHttpClient()
                    .newBuilder()
                    .pingInterval(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build()
            )
            .needReconnect(true)
            .wsUrl("ws://xuanyun.chaidog.cn:8992/open/6")
            .build();
        wsManager!!.setWsStatusListener(object : WsStatusListener() {
            override fun onOpen(response: Response?) {
                super.onOpen(response)
                Log.e("WebSocketStatusListener", "链接成功")
            }

            override fun onMessage(text: String?) {
                super.onMessage(text)
                if (!isOperation) {
                    text?.let {
                        Log.e("WebSocketStatusListener", it)
                        val pathContainer = PathContainer();
                        pathList.clear();
                        val pointBean = Gson().fromJson(it, PointBean::class.java)
                        if (null != pointBean) {
                            val instructions = pointBean.instructions;
                            if (instructions.equals(MarkPath.ACTION_ADD) || instructions.equals(MarkPath.ACTION_ERASER)) {
                                if (null != pointBean.pointXs) {
                                    for (i in 0 until pointBean.pointXs.size) {
                                        pathList.add(Coordinate(pointBean.pointXs[i].toDouble(), pointBean.pointYs[i].toDouble()))
                                    }
                                    pathContainer.latestPath = ArrayList(pathList)
                                    if (instructions.equals(MarkPath.ACTION_ADD)) {
                                        pathContainer.drawMode = BlankPhotoView.DrawMode.path;
                                        dataBinding.photoView.drawPath(pathContainer)
                                    } else {
                                        pathContainer.drawMode = BlankPhotoView.DrawMode.eraser;
                                        dataBinding.photoView.drawEraserPath(pathContainer);
                                    }
                                }
                            } else if (instructions.equals(MarkPath.ACTION_SCALE)) {
                                val mScaleBean = pointBean.mScaleBean
                                dataBinding.photoView.setDrawMode(BlankPhotoView.DrawMode.normal)
                                dataBinding.photoView.apply {
                                    setScale(
                                        mScaleBean.scaleFactor, MyPathUtils.parseRatioToX(mScaleBean.focusX, this),
                                        MyPathUtils.parseRatioToY(mScaleBean.focusY, this), true
                                    )
                                }

                            } else if (instructions.equals(MarkPath.ACTION_REVERT)) {
                                dataBinding.photoView.revert()

                            } else if (instructions.equals(MarkPath.ACTION_CLEAR)) {
                                dataBinding.photoView.clearPath()

                            } else if (instructions.equals(MarkPath.ACTION_ROTATE)) {
                                dataBinding.photoView.rotateTo(pointBean.rotate)
                            } else if (instructions.equals(MarkPath.ACTION_MOVE)) {
                                dataBinding.photoView.apply {
                                    postDrag(
                                        MyPathUtils.parseRatioToX(pointBean.mMoveBean.dx, this),
                                        MyPathUtils.parseRatioToY(pointBean.mMoveBean.dy, this)
                                    )
                                }

                            } else if (instructions.equals(MarkPath.ACTION_FLY)) {
                                dataBinding.photoView.apply {
                                    setFlip(
                                        MyPathUtils.parseRatioToX(pointBean.mMoveBean.velocityX.toFloat(), this).toInt(),
                                        MyPathUtils.parseRatioToY(pointBean.mMoveBean.velocityX.toFloat(), this).toInt()
                                    );
                                }

                            } else if (instructions.equals(MarkPath.ACTION_SHADE)) {
                                dataBinding.shadeView.apply {
                                    height = pointBean.shadeHeight
                                    visibility = View.VISIBLE;
                                }


                            }

                        }
                    }
                }

            }

            override fun onMessage(bytes: okio.ByteString?) {
                super.onMessage(bytes)
            }

            override fun onClosing(code: Int, reason: String?) {
                super.onClosing(code, reason)
                Log.e("WebSocketStatusListener", "正在断开链接")
            }

            override fun onClosed(code: Int, reason: String?) {
                super.onClosed(code, reason)
                Log.e("WebSocketStatusListener", "已断开链接")
            }

            override fun onFailure(t: Throwable?, response: Response?) {
                super.onFailure(t, response)
                Log.e("WebSocketStatusListener", "链接失败")
            }
        })
        wsManager!!.startConnect()

    }

}