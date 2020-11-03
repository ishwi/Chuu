import {IImageQueue} from "../../types/backend.types";

const QueuedImage: React.FC<{ image: IImageQueue }> = ({image}) =>
    <img
        src={image.url}
        alt="Picture of the author"
    />
export default QueuedImage;

