import css from './index.module.scss';

import {NextPageContext} from 'next';
import * as React from 'react';

import TokenService from '../../services/Token.service';

import PageContent from '../../components/PageContent';

function Dashboard() {
  return (
      <PageContent>
        <h2 className={css.example}>Dash!</h2>
      </PageContent>
  );
}

Dashboard.getInitialProps = async (ctx: NextPageContext) => {
  const tokenService = new TokenService();
  await tokenService.authenticateTokenSsr(ctx);

  return {};
};

export default Dashboard;
