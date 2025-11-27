# Extracción de archivos OSRM

Descargar archivo PBF para Guatemala.

https://download.geofabrik.de/central-america/guatemala-latest.osm.pbf

## Windows

- Extraer el PBF
```
docker run --rm -v ${PWD}/osrm:/data osrm/osrm-backend:latest osrm-extract -p /opt/car.lua /data/guatemala.osm.pbf
```

- Contractar
```
docker run --rm -v ${PWD}/osrm:/data osrm/osrm-backend:latest osrm-contract /data/guatemala.osrm
```

## Ubuntu - Producción 

```
docker run --rm -v ~/subiteya-backend/osrm:/data osrm/osrm-backend \
    osrm-extract -p /opt/car.lua /data/guatemala.osm.pbf
```


```
docker run --rm -v ~/subiteya-backend/osrm:/data osrm/osrm-backend \
    osrm-contract /data/guatemala.osrm
```
