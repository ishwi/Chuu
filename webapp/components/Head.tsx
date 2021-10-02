import Head from 'next/head';
import * as React from 'react';
import css from './Head.module.css';
import { useGlobalMessaging } from '../services/GlobalMessaging.context';
import useLocalStorage from '../services/UseLocalStorage';

interface IProps {
}

const Header = (props: IProps) => {
  const [
    globalMessaging,
    messageDispatch,
  ] = useGlobalMessaging();
  const [
    userInfStr,
    setter,
  ] = useLocalStorage('userInfo', null);
  const userInfo = JSON.parse(userInfStr);

  return (
    <div className={css.header}>
      <div className={css.inner}>
        <Head>
          <title>Next.js, Typescript and JWT boilerplate</title>
          <meta
            content="initial-scale=1.0, width=device-width"
            name="viewport"
          />
        </Head>
        {userInfo?.discordName
				  ? <div />
				  : null}
        {globalMessaging.message
				  ? <p className="globalStatus">{globalMessaging.message}</p>
				  : null}
        <h1 className="h1">Next.js, Typescript and JWT boilerplate</h1>
      </div>
    </div>
  );
};

export default Header;
