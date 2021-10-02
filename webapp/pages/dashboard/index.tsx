import { NextPageContext } from 'next';
import * as React from 'react';
import css from './index.module.scss';

import PageContent from '../../components/PageContent';

const Dashboard = () => (
  <PageContent>
    <h2 className={css.example}>Dash!</h2>
  </PageContent>
);

Dashboard.get = async (ctx: NextPageContext) =>

/*
 * Const tokenService = new TokenService();
 * Await tokenService.authenticateTokenSsr(ctx);
 */

  ({});

export default Dashboard;
