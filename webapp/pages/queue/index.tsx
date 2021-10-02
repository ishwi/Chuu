import useSWR from 'swr';
import { useCallback, useState } from 'react';
import { ChuuApi } from '../../services/api/ChuuApi';
import { IImageQueue } from '../../types/backend.types';

import SelectedImage from '../../components/queued-images/Select';

const ImageQueueList: React.FC = () => {
  const { data } = useSWR<IImageQueue[]>([{ limit: 10 }], new ChuuApi().baseApi.fetchNewImages);
  const [
    selectAll,
    setSelectAll,
  ] = useState(false);
  const [
    currentImage,
    setCurrentImage,
  ] = useState(0);
  const [
    viewerIsOpen,
    setViewerIsOpen,
  ] = useState(false);

  const openLightbox = useCallback((event, { _photo, index }) => {
    setCurrentImage(index);
    setViewerIsOpen(true);
  }, []);

  const closeLightbox = () => {
    setCurrentImage(0);
    setViewerIsOpen(false);
  };
  const imageRenderer = useCallback(
    ({
      index, left, top, key, photo,
    }) => (
      <SelectedImage
        direction="row"
        index={index}
        key={key}
        left={left}
        margin="2px"
        onclick={openLightbox}
        photo={photo}
        selected={selectAll}
        top={top}
      />
    ),
    [selectAll],
  );

  console.log(data);
  if (data) {
    const photos = data.map((x) => ({
      src: x.url,
      width: 1,
      height: 1,
    }));

    const toggleSelectAll = () => {
      setSelectAll(!selectAll);
    };

    return (

      <div>
        {/* <Gallery */}
        {/*  Direction="row" */}
        {/*  OnClick={openLightbox} */}
        {/*  Photos={photos} */}
        {/*  TargetRowHeight={400} */}
        {/* /> */}
        <ModalGateway>
          {/* Export interface ViewType {
  caption?: React.ReactNode;
  source: string | {
    download?: string;
    fullscreen?: string;
    regular: string;
    thumbnail?: string;
  };
} */}
          {viewerIsOpen
					  ? (
  <Modal onClose={closeLightbox}>
    <Carousel
      currentIndex={currentImage}
      views={[]}
    />
  </Modal>
            )
					  : null}
        </ModalGateway>
        <p>
          <button onClick={toggleSelectAll}>toggle select all</button>
        </p>
      </div>
    );
  }

  return <div>error</div>;
};

export default ImageQueueList;
