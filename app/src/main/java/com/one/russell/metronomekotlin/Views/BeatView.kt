package com.one.russell.metronomekotlin.Views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.one.russell.metronomekotlin.BeatType
import com.one.russell.metronomekotlin.R
import com.one.russell.metronomekotlin.Utils


class BeatView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    val greyPaint = Paint()
    val whitePaint = Paint()
    var accentBoxHeight = 0f
    var beatType = BeatType.BEAT
    var bordersImage: Bitmap? = null
    var filter = ColorFilter()
    var borderWidth = Utils.getPixelsFromDp(2).toFloat()
    var fillRectangle = RectF()
    private var leftSideMatrix = Matrix()
    private var topSideMatrix = Matrix()
    private var rightBorderPostiton = 0f
    private var sideBorder = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
    private var bottomBorder = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

    constructor(context: Context, beatType: BeatType) : this(context) {
        this.beatType = beatType
    }

    init {
        leftSideMatrix.preScale(-1f, 1f)
        topSideMatrix.preScale(1f, -1f)

        minimumWidth = 100
        /*Glide.with(this)
                .asDrawable()
                .load(R.drawable.buttons_img)
                .into(this@BeatView)
        val bordersImage = Bitmap.createBitmap(this.measuredWidth, this.measuredHeight, Bitmap.Config.ARGB_8888)
        val chunk = bordersImage.ninePatchChunk

        NinePatchDrawable.createFromStream()
        NinePatchBitmapFactory.*/
        //var ninePatch = NinePatchDrawable()
        //setBackgroundResource(R.drawable.qweasd)


        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                viewTreeObserver.removeOnPreDrawListener(this)
                accentBoxHeight = when (beatType) {
                    BeatType.ACCENT -> 0f
                    BeatType.SUBACCENT -> (measuredHeight / 2).toFloat()
                    BeatType.BEAT -> measuredHeight.toFloat() - borderWidth * 2
                    else -> (measuredHeight / 4).toFloat()
                }
                fillRectangle = RectF(borderWidth, accentBoxHeight + borderWidth, measuredWidth.toFloat() - borderWidth, measuredHeight.toFloat() - borderWidth)

                /*val drawable = context.resources.getDrawable(R.drawable.frame)

                bordersImage = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bordersImage)
                drawable.setBounds(0, 0, measuredWidth, measuredHeight)
                drawable.draw(canvas)*/

                /*Glide.with(this@BeatView)
                        .asDrawable()
                        .load(R.drawable.buttons_img)
                        .into(bordersImage)*/
                //val bordersImage = BitmapFactory.decodeResource(context.resources, R.drawable.buttons_img)
                //val bordersImage = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)

                //val qwe = NinePatchBitmapFactory.createNinePatchDrawable(Resources.getSystem(), bordersImage)

                //setImageBitmap(bordersImage)
                Glide.with(getContext())
                        .asBitmap()
                        .load(R.drawable.buttonside)
                        .into(object : SimpleTarget<Bitmap>(1, measuredHeight) {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                sideBorder = resource
                                leftSideMatrix.postTranslate((resource.width).toFloat(), 0f)
                                rightBorderPostiton = (measuredWidth - sideBorder.width).toFloat()

                                Glide.with(getContext())
                                        .asBitmap()
                                        .load(R.drawable.buttonsbottom)
                                        .into(object : SimpleTarget<Bitmap>(1, sideBorder.width) {
                                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                                bottomBorder = resource
                                                topSideMatrix.postTranslate(0f, (resource.height).toFloat())
                                                invalidate()
                                            }
                                        })
                            }
                        })



                return true
            }
        })
        greyPaint.color = Color.GRAY
        whitePaint.color = Color.WHITE
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        beatType = when (beatType) {
            BeatType.BEAT -> BeatType.SUBACCENT
            BeatType.SUBACCENT -> BeatType.ACCENT
            BeatType.ACCENT -> BeatType.MUTE
            BeatType.MUTE -> BeatType.BEAT
        }

        val positionAnimator = when (beatType) {
            BeatType.ACCENT -> ValueAnimator.ofFloat((measuredHeight / 2).toFloat(), 0f)
            BeatType.SUBACCENT -> ValueAnimator.ofFloat(measuredHeight.toFloat(), (measuredHeight / 2).toFloat())
            BeatType.BEAT -> ValueAnimator.ofFloat((measuredHeight / 4).toFloat(), (measuredHeight - borderWidth * 2).toFloat())
            else -> ValueAnimator.ofFloat(0f, (measuredHeight / 4).toFloat())
        }

        //positionAnimator.duration = duration.toLong()
        positionAnimator.interpolator = DecelerateInterpolator()
        positionAnimator.addUpdateListener { animation ->
            accentBoxHeight = animation.animatedValue as Float
            fillRectangle = RectF(borderWidth, accentBoxHeight + borderWidth, measuredWidth.toFloat() - borderWidth, measuredHeight.toFloat() - borderWidth)
            invalidate()
        }

        positionAnimator.start()

        return super.onTouchEvent(event)
    }

    fun startColorAnimation() {
        val colorAnimator = ValueAnimator.ofInt(0, 255)
        colorAnimator.interpolator = DecelerateInterpolator()
        colorAnimator.addUpdateListener { animation ->
            val animatorValue = (animation.animatedValue as Int)
            filter = when (beatType) {
                BeatType.ACCENT -> {
                    val redColor = Color.rgb(255, animatorValue, animatorValue)
                    PorterDuffColorFilter(redColor, PorterDuff.Mode.MULTIPLY)
                }
                BeatType.SUBACCENT -> {
                    val greenColor = Color.rgb(animatorValue, 255, animatorValue)
                    PorterDuffColorFilter(greenColor, PorterDuff.Mode.MULTIPLY)
                }
                BeatType.BEAT -> {
                    val blueColor = Color.rgb(animatorValue, animatorValue, 255)
                    PorterDuffColorFilter(blueColor, PorterDuff.Mode.MULTIPLY)
                }
                else -> {
                    colorAnimator.setIntValues(122, 255)
                    val grayColor = Color.rgb(animatorValue, animatorValue, animatorValue)
                    PorterDuffColorFilter(grayColor, PorterDuff.Mode.MULTIPLY)
                }
            }
            invalidate()
        }

        colorAnimator.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d("asd", "onMeasure")
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas?) {
        greyPaint.colorFilter = filter
        whitePaint.colorFilter = filter
        //todo всё ломается при изменении количества ударов в такте
        //canvas?.drawRoundRect(RectF(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat()), Utils.getPixelsFromDp(5).toFloat(), Utils.getPixelsFromDp(5).toFloat(), whitePaint)
        canvas?.drawRoundRect(fillRectangle, Utils.getPixelsFromDp(5).toFloat(), Utils.getPixelsFromDp(5).toFloat(), greyPaint)
        /*if(bordersImage != null) {
            canvas?.drawBitmap(bordersImage, 0f, 0f, Paint())
        }*/
        //canvas?.drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), greyPaint)



        val rect = RectF(sideBorder.width.toFloat(),0f,0f,sideBorder.height.toFloat())
        val rectsrc = Rect(0,0,width,height)
        val rectTop = RectF(sideBorder.width.toFloat(),bottomBorder.height.toFloat(),(width - sideBorder.width).toFloat(),0f)
        val rectsrcTop = Rect(0,0,sideBorder.width,sideBorder.height)
        val rectBottom = RectF(sideBorder.width.toFloat(),height - bottomBorder.height.toFloat(),(width - sideBorder.width).toFloat(),height.toFloat())
        val rectsrcBottom = Rect(0,0,sideBorder.width,sideBorder.height)
        //canvas?.drawBitmap(bottomBorder, sideBorder.width.toFloat(), (height - bottomBorder.height).toFloat(), whitePaint)
        //canvas?.drawBitmap(bottomBorder, topSideMatrix, whitePaint)
        canvas?.drawBitmap(bottomBorder, rectsrc, rectTop, whitePaint)
        canvas?.drawBitmap(bottomBorder, rectsrc, rectBottom, whitePaint)
        canvas?.drawBitmap(sideBorder, rectsrc, rect, whitePaint)
        //canvas?.drawBitmap(sideBorder, leftSideMatrix, whitePaint)
        canvas?.drawBitmap(sideBorder, (width - sideBorder.width).toFloat(), 0f, whitePaint)

        super.onDraw(canvas)
    }
}