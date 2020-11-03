import ICommand from "../../types/backend.types";

const DisplayCommand: React.FC<{ command: ICommand }> = (props) =>
    <div>
        <p>{props.command.name}</p>
        <p>{props.command.aliases.join(" ")}</p>
        <p>{props.command.category}</p>
        <p>{props.command.instructions}</p>
    </div>
export default DisplayCommand;

