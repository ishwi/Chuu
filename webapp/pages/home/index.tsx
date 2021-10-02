import Link from 'next/link';
import Router from 'next/router';
import * as React from 'react';
import { NextPageContext } from 'next';

import PageContent from '../../components/PageContent';

interface IProps {
    action: string;
}

const Home = (props: IProps) => {
  /*
	 * Log user out when they are directed to the /l=t URL - caught in the getInitialProps at the
	 * Bottom of the page
	 */
  React.useEffect(() => {
    if (props.action && props.action == 'logout') {
      Router.push('/');
    }
  }, []);

  return (
    <PageContent>
      <div>
        <Link
          href="https://discord.com/api/oauth2/authorize?client_id=537353774205894676&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Flogin&response_type=code&scope=identify&state=http://localhost:3000/DiscordLogin"
        >
          <a>HOME</a>
        </Link>
        <div>
          Click
          {' '}
          <Link href="/about">
            <a>here</a>
          </Link>
          {' '}
          to read more
        </div>
        <div>
          Click
          {' '}
          <Link href="/register">
            <a>here</a>
          </Link>
          {' '}
          to register
        </div>
      </div>
    </PageContent>
  );
};

Home.getInitialProps = async (ctx: NextPageContext) => {
  if (ctx.query && ctx.query.l == 't') {
    return { action: 'logout' };
  }

  return {};
};

export default Home;
