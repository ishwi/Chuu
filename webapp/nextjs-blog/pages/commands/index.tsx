import {GetStaticProps,} from 'next'
import ICommand from "../../types/backend.types";
import DisplayCommand from "../../components/commands/Command";


const CommandList: React.FC<{ commands: ICommand[] }> = (props) =>
    <ul>
        {props.commands.map((post) => (
            <li><DisplayCommand command={post}/></li>
        ))}
    </ul>
export default CommandList;


export const getStaticProps: GetStaticProps = async (context) => {
    // Call an external API endpoint to get posts
    console.log('you there')
    let input = `http://${process.env.API_HOST}:${process.env.API_PORT}/commands`;
    console.log(input)
    const res = await fetch(input);
    const commands = await res.json()
    console.log(commands)
    // By returning { props: posts }, the Blog component
    // will receive `posts` as a prop at build time
    return {
        props: {
            commands,
        },
        revalidate: 1000
    }
}
