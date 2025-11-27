package com.example.public_transport_app.ui.imageSlider

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.viewpager2.widget.ViewPager2
import com.example.public_transport_app.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Vista personalizada que muestra un slider de imágenes con indicador
 * y desplazamiento automático opcional.
 *
 * @constructor Crea un ImageSliderView inflando su layout.
 *
 * @param context Contexto de la vista.
 * @param attrs Atributos XML opcionales.
 * @param defStyleAttr Estilo por defecto.
 */
class ImageSliderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val viewPager: ViewPager2
    private val tabLayout: TabLayout
    private lateinit var adapter: ImageSliderAdapter

    init {
        LayoutInflater.from(context).inflate(R.layout.view_image_slider, this, true)
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
    }

    /**
     * Configura las imágenes del slider.
     *
     * @param images Lista de URLs de imágenes.
     */
    fun setImages(images: List<String>) {
        adapter = ImageSliderAdapter(images)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
    }
}
