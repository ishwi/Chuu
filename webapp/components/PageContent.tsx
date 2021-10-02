import * as React from 'react';
import css from './PageContent.module.css';

import Header from './Head';

const PageContent = ({ children }: any) => (
  <>
    <Header />
    <div className={css.pageContent}>{children}</div>
  </>
);

export default PageContent;
