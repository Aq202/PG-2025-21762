import { latLngToCell, gridDisk } from "h3-js";

const getH3Cell = ({
    location,
    resolution,
}:{
    location: LatLong;
    resolution: number;
}): string => {
    return latLngToCell(location.lat, location.long, resolution);
};

const getH3CellWithNeighbors = ({
    location,
    resolution,
    kNeighbors
}:{
    location: LatLong;
    resolution: number;
    kNeighbors: number;
}): string[] => {
    const centerCell = getH3Cell({ location, resolution });
    const neighbors = gridDisk(centerCell, kNeighbors);

    return [centerCell, ...neighbors];
}

export {
    getH3Cell,
    getH3CellWithNeighbors
};