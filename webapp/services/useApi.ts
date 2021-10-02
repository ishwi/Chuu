import useSWR from 'swr';
import { DefaultApi } from './api';
import ChuuApi from './api/ChuuApi';
import useLocalStorage from './UseLocalStorage';

export default function useAPi(): DefaultApi {
  const [token, _setter] = useLocalStorage('token', 'INVALID_TOKEN');
  console.log(token);
  const { data } = useSWR<DefaultApi>(token, (paramToken) => new ChuuApi(paramToken).baseApi);
  console.log(data);
  return data;
}
