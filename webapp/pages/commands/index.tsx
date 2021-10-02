import useSWR from 'swr';
import ICommand from '../../types/backend.types';
import DisplayCommand from '../../components/commands/Command';
import { ChuuApi } from '../../services/api/ChuuApi';
import { CommandInfo } from '../../services/api';

const CommandList: React.FC<{ commands: ICommand[] }> = (props) => (
  <ul>
    {props.commands.map((post) => <li key={post.name}><DisplayCommand command={post} /></li>)}
  </ul>
);

export default CommandList;

export async function getStaticProps(context) {
  // Call an external API endpoint to get posts
  console.log('you there');

  const { data } = useSWR<CommandInfo[]>([{ limit: 10 }], new ChuuApi().baseApi.getCommands);

  console.log(data);

  return {
    props: {
      data,
    },
    revalidate: 1000,
  };
}
