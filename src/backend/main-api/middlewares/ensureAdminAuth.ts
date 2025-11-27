import consts from '../utils/consts.js';
import ensureAccessTokenAuth from './ensureAccessTokenAuth.js';

const ensureAdminAuth = ensureAccessTokenAuth(
  [consts.roles.admin],
  'No se cuenta con los privilegios necesarios de administrador.',
);
export default ensureAdminAuth;
