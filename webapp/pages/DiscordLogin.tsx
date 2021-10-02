import { useRouter } from 'next/router';
import useSWR from 'swr';
import { useEffect } from 'react';
import { AccessRefreshToken, DiscordDetails } from '../services/api';
import { ChuuApi } from '../services/api/ChuuApi'; import useAPi from '../services/useApi'; import useLocalStorage from '../services/UseLocalStorage';

const DiscordLogin = () => {
  const router = useRouter();
  const api = useAPi();
  const { uuid } = router.query;

  console.log(uuid, api);
  const {
    data,
    error,
  } = useSWR<AccessRefreshToken>(uuid, (paramUuid) => api.login({ uuid: paramUuid as string }));

  useEffect(() => {
    if (data) {
      console.log('Running effect', data);
      localStorage.setItem('token', data.accessToken);
    }
  }, [data]);

  const {
    data: principal,
    error: principalError,
  } = useSWR<DiscordDetails>(() => data.accessToken, () => api.login2());

  useEffect(() => {
    if (data) {
      console.log('Running effect Principal', data.accessToken);
    }
  }, [data]);

  if (principalError) {
    return <div>{JSON.stringify(error)}</div>;
  }
  if (!principal) {
    return <div>no data</div>;
  }
  if (principal) {
    return <div>{JSON.stringify(data)}</div>;
  }

  return (
    principal
      ? <div>he</div>
      : (
        <div>
          Dick a
        </div>
      )

  );
};

export default DiscordLogin;
