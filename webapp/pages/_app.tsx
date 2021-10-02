import '../public/styles.css';
import 'bootstrap/dist/css/bootstrap.css';
import { GlobalMessagingProvider } from '../services/GlobalMessaging.context';

// This default export is required in a new `pages/_app.js` file.
export default function MyApp({ Component, pageProps }) {
  return (
  // <AuthProvider>
    <GlobalMessagingProvider>
      <Component {...pageProps} />
    </GlobalMessagingProvider>
  // </AuthProvider>
  );
}
