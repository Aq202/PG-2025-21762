import consts from '../utils/consts.js';
import ensureAccessTokenAuth from './ensureAccessTokenAuth.js';

const ensureAgencyAdminAuth = ensureAccessTokenAuth(
  [consts.roles.admin, consts.roles.agencyAdmin],
  'No se cuenta con los privilegios necesarios de administrador de agencia.',
);
export default ensureAgencyAdminAuth;
