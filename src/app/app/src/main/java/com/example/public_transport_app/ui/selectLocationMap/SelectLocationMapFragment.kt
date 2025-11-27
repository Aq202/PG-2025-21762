package com.example.public_transport_app.ui.selectLocationMap

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.public_transport_app.R
import com.example.public_transport_app.databinding.FragmentSelectLocationMapBinding
import com.example.public_transport_app.ui.shared.permissions.LocationPermissionManager
import com.example.public_transport_app.ui.shared.toolbar.BaseToolbarFragment
import com.example.public_transport_app.utils.GuatemalaCity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class SelectLocationMapFragment : BaseToolbarFragment(), OnMapReadyCallback {

    private var _contentBinding: FragmentSelectLocationMapBinding? = null
    private val contentBinding get() = _contentBinding!!

    private val args: SelectLocationMapFragmentArgs by navArgs()
    private lateinit var locationPermissionManager: LocationPermissionManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var googleMap: GoogleMap
    private var selectedLatLng: LatLng? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val parent = super.onCreateView(inflater, container, savedInstanceState)
        _contentBinding = FragmentSelectLocationMapBinding.inflate(inflater, binding.contentContainer, true)

        locationPermissionManager = LocationPermissionManager(requireContext(), this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        return parent
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = getString(R.string.select_location_title)

        val mapFragment = childFragmentManager.findFragmentById(
            contentBinding.fragmentContainerSelectLocationFragmentDialogMap.id
        ) as SupportMapFragment
        mapFragment.getMapAsync(this)

        contentBinding.buttonSelectLocationMapFragmentConfirm.setOnClickListener {
            if (selectedLatLng == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.select_location_before_error),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val latLng = selectedLatLng!!
            val result = Bundle().apply {
                putDouble("lat", latLng.latitude)
                putDouble("lng", latLng.longitude)
            }

            setFragmentResult(args.requestKey, result)
            findNavController().popBackStack()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        requestLocationAndMoveCamera()

        googleMap.setOnMapClickListener { latLng ->
            selectedLatLng = latLng
            googleMap.clear()
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.location_selected))
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationAndMoveCamera() {
        val fallbackLocation = GuatemalaCity

        locationPermissionManager.requestApproximateLocation(
            rationaleTitle = "Necesitamos saber dónde te encuentras",
            rationaleMessage = "Para ayudarte a elegir una ubicación, necesitamos saber dónde estás. Así podemos mostrarte el mapa cerca de tu zona, y no en otro país por accidente.",
        ) { isGranted ->
            if (isGranted) {
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = true

                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    val userLocation = location?.let {
                        LatLng(it.latitude, it.longitude)
                    } ?: fallbackLocation

                    moveCameraTo(userLocation)
                }.addOnFailureListener {
                    moveCameraTo(fallbackLocation)
                }

            } else {
                Toast.makeText(
                    requireContext(),
                    "No se pudo obtener tu ubicación. Se usará una ubicación por defecto.",
                    Toast.LENGTH_SHORT
                ).show()
                moveCameraTo(fallbackLocation)
            }
        }
    }

    private fun moveCameraTo(location: LatLng) {
        val camera = CameraPosition.Builder()
            .target(location)
            .zoom(15.0f)
            .build()

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(camera))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _contentBinding = null
    }

    override fun onCreateCustomMenu(menu: Menu, menuInflater: MenuInflater) {
        // No inflar menú para este fragmento
    }
}
