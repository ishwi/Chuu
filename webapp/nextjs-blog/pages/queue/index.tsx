import {IImageQueue} from "../../types/backend.types";
import useSWR from "swr";
import fetchGet from "../../services/Fetch.service";
import Carousel, {Modal, ModalGateway} from "react-images";

import Gallery from "react-photo-gallery";
import {useCallback, useState} from "react";
import SelectedImage from "../../components/queued-images/Select";


const ImageQueueList: React.FC = () => {
    const {data} = useSWR<IImageQueue[]>('/image-queue?limit=10', fetchGet)
    const [selectAll, setSelectAll] = useState(false);
    const [currentImage, setCurrentImage] = useState(0);
    const [viewerIsOpen, setViewerIsOpen] = useState(false);

    const openLightbox = useCallback((event, {photo, index}) => {
        setCurrentImage(index);
        setViewerIsOpen(true);
    }, []);

    const closeLightbox = () => {
        setCurrentImage(0);
        setViewerIsOpen(false);
    };
    const imageRenderer = useCallback(
        ({index, left, top, key, photo}) => (
            <SelectedImage
                onclick={openLightbox}
                direction={'row'}
                selected={selectAll}
                key={key}
                margin={"2px"}
                index={index}
                photo={photo}
                left={left}
                top={top}
            />
        ),
        [selectAll]
    );

    console.log(data);
    if (data) {
        let photos = data.map(x => ({src: x.url, width: 1, height: 1}));

        const toggleSelectAll = () => {
            setSelectAll(!selectAll);
        };


        return (

            <div>
                <Gallery onClick={openLightbox} photos={photos} targetRowHeight={400} direction={"row"}/>
                <ModalGateway>
                    {viewerIsOpen ? (
                        <Modal onClose={closeLightbox}>
                            <Carousel
                                components={{Footer: (<p>a</p>)}
                                    currentIndex={currentImage}
                                    views={photos.map(x => ({
                                    ...x,
                                    srcset: x.src,
                                    caption: x.src
                                }))}
                                    />
                                    </Modal>
                                    ) : null}
                                    </ModalGateway>
                                    <p>
                                    <button onClick={toggleSelectAll}>toggle select all</button>
                                    </p>
                                    </div>
                                    );
                                    }
                                    return (<div>error</div>)
                                    }
                                    export default ImageQueueList;



