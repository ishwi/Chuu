interface ICommand {
    name: string,
    category: string,
    instructions: string,
    aliases: string[]
}

export interface IImageQueue {
    url: string
}


export default ICommand;
