import { ImageQueue } from '../../services/api';

const QueuedImage = ({ image } : {image: ImageQueue}) => (
  <img
    alt="author"
    src={image.url}
  />
);
export default QueuedImage;
